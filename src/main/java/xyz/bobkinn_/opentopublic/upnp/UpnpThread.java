package xyz.bobkinn_.opentopublic.upnp;

import com.dosse.upnp.UPnP;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.bobkinn_.opentopublic.OpenToPublic;
import xyz.bobkinn_.opentopublic.PortContainer;
import xyz.bobkinn_.opentopublic.Util;

import java.util.ArrayList;

public class UpnpThread extends Thread{
    public final boolean doSetup;
    public UpnpThread(boolean doSetup){
        super("UpnpWorksThread");
        this.doSetup = doSetup;
        setUncaughtExceptionHandler(new Handler());
    }

    @Override
    public void run() {
        if (doSetup) setup();
        else doClose();
    }

    public static class Handler implements UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            if (!(e instanceof UpnpEx)) {
                OpenToPublic.LOGGER.error("Exception in UpnpWorksThread: ", e);
                return;
            }
            UpnpEx err = (UpnpEx) e;
            if (err.getType() == UpnpEnum.CHECK_AVAILABLE){
                Util.addChatMsg(Text.translatable("opentopublic.message.upnp_not_available").formatted(Formatting.RED));
                OpenToPublic.LOGGER.error(err.getEx());
            } else if (err.getType() == UpnpEnum.FAIL_GET_IP) {
                OpenToPublic.LOGGER.error("Failed to get ip:", e);
                Util.addChatMsg(Text.translatable("opentopublic.publish.failed_wan").formatted(Formatting.RED));
            } else if (err.getType() == UpnpEnum.OPEN_PORT) {
                Util.addChatMsg(Text.translatable("opentopublic.publish.failed_wan").formatted(Formatting.RED));
            }
        }
    }

    public static void runSetup(){
        UpnpThread t = new UpnpThread(true);
        t.setName("UpnpSetupThread");
        t.start();
    }

    public static void runClose(){
        UpnpThread t = new UpnpThread(false);
        t.setName("UpnpCloseThread");
        t.start();

    }

    public void doClose(){
        try {
            boolean available = UPnP.isUPnPAvailable();
            if (!available) throw new RuntimeException("Failed isUPnPAvailable()");
        } catch (Exception e){
            throw new UpnpEx(UpnpEnum.CHECK_AVAILABLE, e);
        }

        OpenToPublic.LOGGER.info("Closing main port "+OpenToPublic.customPort);
        try {
            if (!UPnP.closePortTCP(OpenToPublic.customPort)) throw new RuntimeException();
            PortContainer.self.mainPort = null;
        } catch (Exception e){
            OpenToPublic.LOGGER.error("Failed close of main port "+OpenToPublic.customPort, e);
        }

        ArrayList<Integer> closedTcp = new ArrayList<>();
        for (int port : PortContainer.self.getTcpPorts()){
            try {
                if (!UPnP.closePortTCP(port)) throw new RuntimeException();
                closedTcp.add(port);
            } catch (Exception e){
                OpenToPublic.LOGGER.error("Failed close additional port "+port+" [TCP]. Maybe it already closed?");
            }
        }
        if (!closedTcp.isEmpty()) OpenToPublic.LOGGER.info("Closed TCP ports: "+closedTcp);
        PortContainer.self.upnpPorts.get("tcp").clear();

        ArrayList<Integer> closedUdp = new ArrayList<>();
        for (int port : PortContainer.self.getUdpPorts()){
            try {
                if (!UPnP.closePortUDP(port)) throw new RuntimeException();
                closedUdp.add(port);
            } catch (Exception e){
                OpenToPublic.LOGGER.error("Failed close additional port "+port+" [UDP]. Maybe it already closed?");
            }
        }
        if (!closedUdp.isEmpty()) OpenToPublic.LOGGER.info("Closed UDP ports: "+closedUdp);
        PortContainer.self.upnpPorts.get("udp").clear();
        if (!OpenToPublic.backupFile.delete()) OpenToPublic.LOGGER.error("Failed to delete container file");
    }

    public void setup() {
        boolean available;
        try {
            available = UPnP.isUPnPAvailable();
            if (!available) throw new RuntimeException("not available");
        } catch (Exception e){
            Util.addChatMsg(Text.translatable("opentopublic.message.upnp_not_available").formatted(Formatting.RED));
            throw new UpnpEx(UpnpEnum.CHECK_AVAILABLE, e);
        }

        try {
            OpenToPublic.upnpSuccess = UPnP.openPortTCP(OpenToPublic.customPort);
            if (!OpenToPublic.upnpSuccess) throw new RuntimeException();
        } catch (Exception e){
            OpenToPublic.LOGGER.error("Failed to open main port: ", e);
            throw new UpnpEx(UpnpEnum.OPEN_PORT, e);
        }

        try {
            OpenToPublic.upnpIp = UPnP.getExternalIP();
            if (OpenToPublic.upnpIp == null) throw new RuntimeException();
        } catch (Exception e){
            throw new UpnpEx(UpnpEnum.FAIL_GET_IP, e);
        }

        Util.atSuccessOpen(true);

        for (int port : PortContainer.self.getTcpPorts()){
            try {
                if (!UPnP.openPortTCP(port)) throw new RuntimeException();
            } catch (Exception e){
                Util.addChatMsg(Text.translatable("opentopublic.message.additional_open_fail", port +" [TCP]").formatted(Formatting.RED));
            }
        }
        for (int port : PortContainer.self.getUdpPorts()){
            try {
                if (!UPnP.openPortUDP(port)) throw new RuntimeException();
            } catch (Exception e){
                Util.addChatMsg(Text.translatable("opentopublic.message.additional_open_fail", port +" [UDP]").formatted(Formatting.RED));
            }
        }

    }
}
