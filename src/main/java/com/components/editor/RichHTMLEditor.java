package com.components.editor;

import com.*;
import com.filters.*;
import com.utils.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

public class RichHTMLEditor extends AbstractEditor {
	private Map<Integer, ImageMetadata> imagesMetadata;
	private Toolbar toolbar;

	private static final String META_DATA_BEGIN = "------------___ IMG METADATA ___------------";

	public RichHTMLEditor(Notepad notepad, JPanel tab, String text) {
		super(notepad, tab, text);
	}

	@Override
	protected void buildUI(String text) {
		imagesMetadata = new HashMap<>();

		setContentType("text/html");

		var iosFont = new Font(AppSettings.get("font_family"), Font.PLAIN, (int) AppSettings.getNum("font_size"));
		putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		setFont(iosFont);
		setFocusable(true);

		//setText(text);
		try {
			deserialize(text);
		} catch (IOException e) {
			Actions.assertDialog(e);
		}

		AbstractDocument doc = (AbstractDocument) getDocument();
		doc.setDocumentFilter(new ImageDeleteFilter(this));
		bindChangeEventToDoc(doc);

		toolbar = new Toolbar(notepad, tab, this);
		toolbar.onChange(changeEvent);
	}

	@Override
	public void serialize(PrintWriter writer) {
		// Set HTML
		writer.println(getText());

		// Set Image metadata
		writer.println(META_DATA_BEGIN);
		for (var entry : imagesMetadata.entrySet()) {
			writer.println(entry.getKey() + "\t" + entry.getValue().toString());
		}
	}

	@Override
	public void deserialize(String text) throws IOException {

		String[] data = text.split(META_DATA_BEGIN);
		String html = data[0];

		// If image metadata
		if (data.length <= 1) {
			setText(html);
			return;
		}

		// Otherwise parseMetadata
		String[] metadata = data[1].split("\n");
		for (var line : metadata) {
			if (line.trim().length() == 0)
				continue;
			String[] tokens = line.split("\t");
			imagesMetadata.put(
					Integer.parseInt(tokens[0].trim()),
					new ImageMetadata(tokens[3].trim(),
							Integer.parseInt(tokens[1].trim()),
							Integer.parseInt(tokens[2].trim()))
			);
		}

		// Now place images
		final String iconTag = "<image (\\$?)ename=\"icon\">";
		html = html.replaceAll(iconTag, "");
		setText(html);

		var imgData = imagesMetadata.entrySet()
				.stream()
				.sorted((a, b) -> Integer.compare(a.getKey(), b.getKey()))
				.toList();

		int total = 0;//iconTag.length() * imgData.size();
		for (var entry : imgData) {
			Image image = base64toImage(entry.getValue().encodeing);
			insertImageAsIcon(image, entry.getKey() - total, entry.getValue().width, entry.getValue().height);
		}
	}

	public void deleteImageAtOffset(int offset) {
		imagesMetadata.remove(offset);
	}

	public void insertImageAsIcon(File file, final int width) {
		// Load the image into an Icon object
		ImageIcon icon = new ImageIcon(file.getAbsolutePath());

		int newWidth = width;
		int newHeight = (int) (icon.getIconHeight() * ((float) newWidth / icon.getIconWidth()));
		icon.setImage(icon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH));

		// Create a style that includes the Icon object
		insertIcon(icon, true, file, getCaretPosition(), newWidth, newHeight);
	}

	public void insertImageAsIcon(Image image, int pos, int width, int height) {
		ImageIcon icon = new ImageIcon(image);
		icon.setImage(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
		insertIcon(icon, false, null, pos, width, height);
	}

	@Override
	public void onChange(Consumer<Object> r) {
		super.onChange(r);
		toolbar.onChange(changeEvent);
	}

	/**
	 * Helper functions
	 */

	private void imageInserted(int caret, File file, int width, int height) throws IOException {
		byte[] fileContent = Files.readAllBytes(file.toPath());
		String encodedString = Base64.getEncoder().encodeToString(fileContent);

		imagesMetadata.put(caret, new ImageMetadata(encodedString, width, height));
	}

	private Image base64toImage(String base64) throws IOException {
		byte[] decodedBytes = Base64.getDecoder().decode(base64);
		return ImageIO.read(new ByteArrayInputStream(decodedBytes));
	}

	private void insertIcon(ImageIcon icon, boolean insertToMetadata, File file, int caretPos, int width, int height) {
		// Create a style that includes the Icon object
		Style style = addStyle("image", null);
		StyleConstants.setIcon(style, icon);
		// Insert the image into the text pane
		try {
			getStyledDocument().insertString(caretPos, "m", style);
			if (insertToMetadata)
				imageInserted(caretPos, file, width, height);
		} catch (Exception ex) {
			Actions.assertDialog(false, ex.getMessage());
		}
	}

	/**
	 * Converts a given Image into a BufferedImage
	 *
	 * @param img The Image to be converted
	 * @return The converted BufferedImage
	 */
	private static BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}

	private static class ImageMetadata {
		final String encodeing;
		final int width, height;

		private ImageMetadata(String encodeing, int width, int height) {
			this.encodeing = encodeing;
			this.width = width;
			this.height = height;
		}

		public String toString() {
			return width + "\t" + height + "\t" + encodeing;
		}
	}
}
