package thecodex6824.thaumcraftfix.client;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;
import thecodex6824.thaumcraftfix.common.util.TranslatableMessage;

public class ThaumcraftFixLoaderException extends CustomModLoadingErrorDisplayException {

    private static final long serialVersionUID = 8633145632682616854L;

    private String translatedTitle = null;
    private String translatedMessage = null;

    public ThaumcraftFixLoaderException(String excMessage, TranslatableMessage... messages) {
	super(excMessage, null);
	translatedTitle = I18n.format("thaumcraftfix.text.loader.loaderError");
	StringBuilder builder = new StringBuilder();
	for (int i = 0; i < messages.length; ++i) {
	    TranslatableMessage m = messages[i];
	    builder.append(I18n.format(m.key, m.args));
	    if (i != messages.length - 1) {
		builder.append('\n');
	    }
	}
	translatedMessage = builder.toString();
    }

    @Override
    public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {}

    @Override
    public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseRelX, int mouseRelY,
	    float tickTime) {

	int y = 35;
	for (String line : fontRenderer.listFormattedStringToWidth(translatedTitle, errorScreen.width)) {
	    errorScreen.drawCenteredString(fontRenderer, TextFormatting.BOLD.toString() + line , errorScreen.width / 2, y, 0xffffff);
	    y += 10;
	}

	y += 20;
	for (String line : fontRenderer.listFormattedStringToWidth(translatedMessage, errorScreen.width)) {
	    errorScreen.drawCenteredString(fontRenderer, line, errorScreen.width / 2, y, 0xeeeeee);
	    y += 10;
	}
    }

}
