package wxji.format.js.actions;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * Our sample action implements workbench action delegate. The action proxy will
 * be created by the workbench and shown in the UI. When the user tries to use
 * the action, this delegate will be created and execution will be delegated to
 * it.
 * 
 * @see IWorkbenchWindowActionDelegate
 */
public class FormatAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	private Reader reader;

	private ScriptEngine engine;

	/**
	 * The constructor.
	 */
	public FormatAction() {
	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		IWorkbenchPage activePage = window.getActivePage();
		if (activePage == null) {
			return;
		}
		IEditorPart activeEditor = activePage.getActiveEditor();
		if (activeEditor == null) {
			return;
		}
		AbstractTextEditor textEditor = null;
		if (activeEditor instanceof AbstractTextEditor) {
			textEditor = (AbstractTextEditor) activeEditor;
		} else {
			return;
		}
		IEditorInput editorInput = textEditor.getEditorInput();
		if (editorInput == null) {
			return;
		}
		IDocument document = textEditor.getDocumentProvider().getDocument(editorInput);
		IFile file = (IFile) editorInput.getAdapter(IFile.class);
		if (file == null) {
			return;
		}
		if (!file.getName().endsWith(".js")) {
			return;
		}
		String content = document.get();
		try {
			document.set(format(content));
		} catch (Exception e) {
			MessageDialog.openError(window.getShell(), "´íÎó", e.getMessage());
		}
	}

	private String format(String s) throws Exception {
		if (engine instanceof Invocable) {
			Invocable invoke = (Invocable) engine;
			String result = null;
			result = (String) invoke.invokeFunction("js_beautify", s, 4, "", null);
			return result;
		}
		return null;
	}

	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after the
	 * delegate has been created.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
		try {
			if (reader != null) {
				reader.close();
			}
		} catch (IOException e) {
			MessageDialog.openError(window.getShell(), "´íÎó", e.getMessage());
		}
	}

	/**
	 * We will cache window object in order to be able to provide parent shell
	 * for the message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;

		ScriptEngineManager manager = new ScriptEngineManager();
		engine = manager.getEngineByName("javascript");
		URL url = FormatAction.class.getResource("/jsbeautify.js");

		reader = null;
		try {
			reader = new InputStreamReader(url.openStream());
			engine.eval(reader);
		} catch (ScriptException e) {
			MessageDialog.openError(window.getShell(), "´íÎó", e.getMessage());
		} catch (IOException e) {
			MessageDialog.openError(window.getShell(), "´íÎó", e.getMessage());
		}
	}
}