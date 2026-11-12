package io.github.carlosm.tabswitcherextreme;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.EditorHistoryManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import javax.swing.*;

public class SwitcherDialog extends DialogWrapper implements KeyEventDispatcher{

	private JPanel panel1;
	private JLabel path;
	private JPanel centerpanel;
	private JPanel bottompanel;

	private int trigger = 0;
	private final BitSet modifiers = new BitSet();
	Project mProject;
	private ListManager mListManager;
	public boolean proceed = true;
	private VirtualFile[] mRecentFiles;

	public ListManager getListManager() {
		return mListManager;
	}


	protected SwitcherDialog( @Nullable Project proj, KeyEvent event ) {
		super(proj);

		panel1.setBackground(UIUtil.getListBackground());
		bottompanel.setBackground(UIUtil.getListBackground());
		centerpanel.setBackground(UIUtil.getListBackground());

		mProject = proj;

		PropertiesComponent props = PropertiesComponent.getInstance();
		String descriptions = props.getValue(Configuration.PROPERTY_DESCRIPTIONS, Configuration.DEFAULT_DESCRIPTIONS);
		String regexes = props.getValue(Configuration.PROPERTY_REGEXES, Configuration.DEFAULT_REGEXES);
		String removers = props.getValue(Configuration.PROPERTY_REMOVERS, Configuration.DEFAULT_REMOVERS);
		Utils.log("Read descriptions: " + descriptions);
		Utils.log("Read regexes: '" + regexes + "'");

		String[] descList = descriptions.split("\n");
		Utils.log("Found " + descList.length + " descriptions");
		String[] regexList = regexes.split("\n");
		Utils.log("Found " + regexList.length + " regexes");
		String[] removerList = removers.split("\n");
		Utils.log("Found " + removerList.length + " removers");

		int nrValids = Math.min(descList.length, regexList.length);

		final List<VirtualFile> files = getFiles(mProject);
		if (files.size() < 2) {
			// no use!
			Utils.log("QUIT!!!");
			proceed = false; // hack to not show the dialog

			return;
		}

        List<VirtualFile> recents = EditorHistoryManager.getInstance(proj).getFileList();
        List<VirtualFile> recentFilesArrayList = new ArrayList<VirtualFile>(recents.size());
		recentFilesArrayList.addAll(recents);

        mListManager = new ListManager(mProject, recentFilesArrayList);
//        mListManager = new ListManager(mProject, EditorHistoryManager.getInstance(proj).getFiles());

		for (int i = 0; i < nrValids; i++) {
			String remover = null;
			if (i < removerList.length) {
				remover = removerList[i];
			}

			getListManager().addListDescription(regexList[i], descList[i], remover);
		}

		getListManager().generateFileLists(files);



		// find current file in the leftmost list
		// todo: find previous file in the leftmost list
		VirtualFile mostRecent = mostRecentFile(proj);

		ListManager.FilePosition initialPos = getListManager().getFilePosition(mostRecent);

//		if (mostRecent != null) {
//			Utils.log("Most recent file: " + mostRecent.getName());
//			int foundListIndex = -1;
//			int foundIndexInList = -1;
//			for (int i =0; i< getListManager().mListDescriptions.size(); i++) {
//				ListManager.ListDescription desc = getListManager().mListDescriptions.get(i);
//				List<VirtualFile> list = desc.mFilteredFileList;
//
//				int index = list.indexOf(mostRecent);
//				if (index != -1) {
//					foundListIndex = i;
//					foundIndexInList = index;
//					break;
//				}
//			}
//			if (foundIndexInList != -1) {
//				initialListIndex = foundListIndex;
//				initialIndexInList = foundIndexInList;
//			}
//		}

		getListManager().mDesiredIndexInList = initialPos.getIndexInList();

		path.setHorizontalAlignment(SwingConstants.RIGHT);
		path.setFont(path.getFont().deriveFont((float) 10));

		getListManager().insertIntoPanel(centerpanel, path);

		getListManager().setActiveListIndex(initialPos.getListIndex());
		getListManager().getActiveList().setSelectedIndex(initialPos.getIndexInList());

		trigger = event.getKeyCode();
		modifiers.set(KeyEvent.VK_CONTROL, event.isControlDown());
		modifiers.set(KeyEvent.VK_META, event.isMetaDown());
		modifiers.set(KeyEvent.VK_ALT, event.isAltDown());
		modifiers.set(KeyEvent.VK_ALT_GRAPH, event.isAltGraphDown());
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);

		init();
	}

	private VirtualFile mostRecentFile(Project proj) {
		//final VirtualFile[] recentFilesArr = getListManager().;
		final FileEditorManager manager = FileEditorManager.getInstance(proj);
		//List<VirtualFile> recentFilesList = new ArrayList<VirtualFile>(recentFilesArr.length);

		//Collections.addAll(recentFilesList, recentFilesArr);
		//Collections.reverse(recentFilesList);

		boolean first = true;
		// loop over it backwards

		int count = getListManager().mRecentFiles.size();

		for (int index = count - 1; index >= 0; index--) {

			VirtualFile file = getListManager().mRecentFiles.get(index);
			if (manager.isFileOpen(file)) {
				if (first) {
					first = false;
				} else {
					return file;
				}
			}
		}
		return null;
	}

	private List<VirtualFile> getFiles(Project project) {

		final FileEditorManager manager = FileEditorManager.getInstance(project);
		final VirtualFile[] allOpenFiles = manager.getOpenFiles();

		List<VirtualFile> newList = new ArrayList<VirtualFile>(allOpenFiles.length);
		Utils.log("All files in project: " + allOpenFiles.length);
		Collections.addAll(newList, allOpenFiles);
		return newList;
	}



	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		return panel1;
	}

	@Override
	public boolean dispatchKeyEvent( KeyEvent event ) {
		boolean consumed = true;
		if ((event.getID() == KeyEvent.KEY_RELEASED) &&
			modifiers.get(event.getKeyCode())) {
			finish(true); // comment this to make screenshots
		} else if (event.getID() == KeyEvent.KEY_PRESSED) {
			final int keyCode = event.getKeyCode();
			if (event.getKeyCode() == trigger) {
				//Utils.log("trigger yo");
				if (event.isShiftDown()) {
					Utils.log("SHIFT IS DOWN");
					getListManager().updateSelection(ListManager.NavigateCommand.SHIFTTAB);
				} else {
					Utils.log("SHIFT IS UP");
					getListManager().updateSelection(ListManager.NavigateCommand.TAB);
				}

//				move(event.isShiftDown());
			} else {
				switch (keyCode) {
					case KeyEvent.VK_UP:
						getListManager().updateSelection(ListManager.NavigateCommand.UP);
						break;
					case KeyEvent.VK_DOWN:
						getListManager().updateSelection(ListManager.NavigateCommand.DOWN);
						break;
					case KeyEvent.VK_RIGHT:
						getListManager().updateSelection(ListManager.NavigateCommand.RIGHT);
						break;
					case KeyEvent.VK_LEFT:
						getListManager().updateSelection(ListManager.NavigateCommand.LEFT);
						break;
					case KeyEvent.VK_PAGE_UP:
						getListManager().updateSelection(ListManager.NavigateCommand.PAGE_UP);
						break;
					case KeyEvent.VK_PAGE_DOWN:
						getListManager().updateSelection(ListManager.NavigateCommand.PAGE_DOWN);
						break;
					case KeyEvent.VK_ENTER:
						finish(true);
						break;
					case KeyEvent.VK_SHIFT:
						break;
					case KeyEvent.VK_CONTROL:
						break;
					case KeyEvent.VK_ALT:
						break;
					case KeyEvent.VK_ALT_GRAPH:
						break;
					case KeyEvent.VK_META:
						break;
					default:
						finish(false);
						break;
				}
			}
		}
		return consumed;
	}

	private void finish(boolean openFile) {
		Utils.log("Quitting");

		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);

		close(0, true);

		if (openFile && mProject != null && !mProject.isDisposed()) {
			ApplicationManager.getApplication().invokeLater(() -> {
				final FileEditorManager editorManager = FileEditorManager.getInstance(mProject);
				final VirtualFile file = getListManager().getSelectedFile();
				if (file != null && file.isValid()) {
					editorManager.openFile(file, true);
				}
			}, ModalityState.nonModal(), mProject.getDisposed());
		}
	}
}
