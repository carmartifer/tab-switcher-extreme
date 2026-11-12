package io.github.carlosm.tabswitcherextreme;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;


public class Configuration implements Configurable {
	public static final String PROPERTY_DESCRIPTIONS = "Descriptions";
	public static final String PROPERTY_REGEXES = "Regexes";
	public static final String PROPERTY_REMOVERS = "Removers";

	public static final String DEFAULT_DESCRIPTIONS = "All (go to Settings | Tools | Tab Switcher Extreme)";
	public static final String DEFAULT_REGEXES = ".*";
	public static final String DEFAULT_REMOVERS = "";

	ConfigurationForm form;

	@Nls
	@Override
	public String getDisplayName() {
		return "TabSwitcher Extreme";
	}

	@Nullable
	@Override
	public String getHelpTopic() {
		return "HALP";
	}

	@Nullable
	@Override
	public JComponent createComponent() {
		form = new ConfigurationForm();
		return form.holder;
	}

	@Override
	public boolean isModified() {
		if (form == null) {
			return false;
		}
		PropertiesComponent props = PropertiesComponent.getInstance();
		String descs = props.getValue(PROPERTY_DESCRIPTIONS, DEFAULT_DESCRIPTIONS);
		String regexes = props.getValue(PROPERTY_REGEXES, DEFAULT_REGEXES);
		String removers = props.getValue(PROPERTY_REMOVERS, DEFAULT_REMOVERS);

		return !Objects.equals(descs, form.textArea1.getText())
				|| !Objects.equals(regexes, form.textArea2.getText())
				|| !Objects.equals(removers, form.textArea3.getText());
	}

	@Override
	public void apply() throws ConfigurationException {
		Utils.log("Apply");
		if (form == null) {
			return;
		}
		PropertiesComponent props = PropertiesComponent.getInstance();
		String descs = form.textArea1.getText();
		String regexes = form.textArea2.getText();
		String removers = form.textArea3.getText();
		props.setValue(PROPERTY_DESCRIPTIONS, descs);
		props.setValue(PROPERTY_REGEXES, regexes);
		props.setValue(PROPERTY_REMOVERS, removers);
	}

	@Override
	public void reset() {
		Utils.log("Reset");
		if (form == null) {
			return;
		}
		PropertiesComponent props = PropertiesComponent.getInstance();
		String descs = props.getValue(PROPERTY_DESCRIPTIONS, DEFAULT_DESCRIPTIONS);
		String regexes = props.getValue(PROPERTY_REGEXES, DEFAULT_REGEXES);
		String removers = props.getValue(PROPERTY_REMOVERS, DEFAULT_REMOVERS);
		Utils.log("Read descriptions: " + descs);
		Utils.log("Read regexes: " + regexes);
		Utils.log("Read removers: " + removers);
		form.textArea1.setText(descs);
		form.textArea2.setText(regexes);
		form.textArea3.setText(removers);
	}

	@Override
	public void disposeUIResources() {
		Utils.log("disposeUIResources");
		form = null;
	}
}
