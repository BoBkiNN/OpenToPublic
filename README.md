[![See on Modrinth - OpenToPublic](https://img.shields.io/badge/See_on_Modrinth-OpenToPublic-2ea44f?logo=modrinth)](https://modrinth.com/mod/opentopublic) [![See on CurseForge - OpenToPublic](https://img.shields.io/badge/See_on_CurseForge-OpenToPublic-orange?logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/opentopublic)
# OpenToPublic
With this mod, you can make "Open to LAN" button open your world to outer network

## Features
 - Enable/Disable online mode
 - Enable/Disable PVP
 - You can set custom port
 - Set server MOTD (Support colors)
 - Set max player value
 - Open to WAN button to open world to outer network (Requires opened port on router if set to Manual)
 - Add additional ports to set when Open to WAN button set to UPnP using config file
 - Option to hide IP addresses when opening to WAN (Streamer mode)

## Menu showcase
![Open To LAN screen](https://cdn.modrinth.com/data/RTCPiKQj/images/b4bd2ab90f8f96db728e1435af9ddea488d5d786.png)

### MOTD placeholders
 - `%owner%` - server owner
 - `%world%` - world name

### Online Mode
If ON, then Minecraft will be check players for license, else players with cracked launchers can join

## More versions?
 \- yes, versions for 1.19.2 forge are planned

## Credit
 - to ChatGPT for helping in development my first mod
 - to [Excal](https://github.com/ae7er) for testing
 - to FasT1k (Discord ID: 631850581580972106) for testing

## config.json explanation
Config path: `.minecraft/config/opentopublic/config.json`\
Example config:
```json
{
  "tcp": [],
  "udp": [
    60606
  ],
  "hideIps": false
}
```
 - `tcp` - list of TCP ports to open if Open to WAN button set to UPnP
 - `udp` - list of UDP ports to open if Open to WAN button set to UPnP
 - `hideIps` - hide IP addresses when opening to WAN? (`true` or `false`)
