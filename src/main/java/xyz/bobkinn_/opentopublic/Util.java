package xyz.bobkinn_.opentopublic;

import net.minecraft.text.TranslatableText;

public class Util {
    static TranslatableText yes = new TranslatableText("options.on");
    static TranslatableText no = new TranslatableText("options.off");
    public static TranslatableText translateYN(boolean bool){
        return bool ? yes : no;
    }
    public static TranslatableText parseYN(String key, boolean onlineMode) {
        return new TranslatableText(key,  Util.translateYN(onlineMode));
    }
}
