package com.utils;

import com.*;
import com.formdev.flatlaf.*;
import com.utils.*;
import mdlaf.*;
import mdlaf.themes.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class ThemeManager {
	private static final Map<String, LookAndFeel> choices = Map.of(
			"Flat dark", new FlatDarkLaf(),
			"Flat light", new FlatLightLaf(),
			"Material Oceanic", new MaterialLookAndFeel(new MaterialOceanicTheme()),
			"Material Lite", new MaterialLookAndFeel(new MaterialLiteTheme())
	);
	protected static Notepad notepad;

	/**
	 * Changes the theme and saves it to the settings
	 *
	 * @param name
	 * @param toRefresh
	 */
	public static void setTheme(String name, Window... toRefresh) {
		try {
			LookAndFeel theme = choices.get(name);
			UIManager.setLookAndFeel(theme);
			SwingUtilities.updateComponentTreeUI(notepad.getFrame());

			notepad.getFrame().pack();

			for (var component : toRefresh) {
				SwingUtilities.updateComponentTreeUI(component);
				component.pack();
			}

			AppSettings.set("theme", name);
		} catch (Exception ex) {
			Actions.assertDialog(false, ex.getMessage());
		}
	}

	public static Map<String, LookAndFeel> getChoices() {
		return choices;
	}
}
