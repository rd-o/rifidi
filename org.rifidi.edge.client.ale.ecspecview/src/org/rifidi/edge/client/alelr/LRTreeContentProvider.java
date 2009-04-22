/**
 * 
 */
package org.rifidi.edge.client.alelr;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;
import org.rifidi.edge.client.ale.api.wsdl.alelr.epcglobal.ALELRServicePortType;
import org.rifidi.edge.client.ale.api.wsdl.alelr.epcglobal.Define;
import org.rifidi.edge.client.ale.api.wsdl.alelr.epcglobal.DuplicateNameExceptionResponse;
import org.rifidi.edge.client.ale.api.wsdl.alelr.epcglobal.EmptyParms;
import org.rifidi.edge.client.ale.api.wsdl.alelr.epcglobal.GetLRSpec;
import org.rifidi.edge.client.ale.api.wsdl.alelr.epcglobal.ImmutableReaderExceptionResponse;
import org.rifidi.edge.client.ale.api.wsdl.alelr.epcglobal.ImplementationExceptionResponse;
import org.rifidi.edge.client.ale.api.wsdl.alelr.epcglobal.InUseExceptionResponse;
import org.rifidi.edge.client.ale.api.wsdl.alelr.epcglobal.NoSuchNameExceptionResponse;
import org.rifidi.edge.client.ale.api.wsdl.alelr.epcglobal.SecurityExceptionResponse;
import org.rifidi.edge.client.ale.api.wsdl.alelr.epcglobal.Undefine;
import org.rifidi.edge.client.ale.api.wsdl.alelr.epcglobal.ValidationExceptionResponse;
import org.rifidi.edge.client.ale.api.xsd.alelr.epcglobal.LRSpec;
import org.rifidi.edge.client.alelr.decorators.LRSpecDecorator;
import org.rifidi.edge.client.alelr.decorators.LRSpecSubnodeDecorator;

/**
 * @author Jochen Mader - jochen@pramari.com
 * 
 */
public class LRTreeContentProvider implements ITreeContentProvider {
	/** Logger for this class. */
	private static final Log logger = LogFactory
			.getLog(LRTreeContentProvider.class);
	/** Viewer this content provider is responsible for. */
	private TreeViewer viewer;
	/** Service port connected to the edge server. */
	private ALELRServicePortType lrService;
	/** Logical readers. */
	private List<LRSpecDecorator> lrspecs;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.
	 * Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ALELRServicePortType) {
			return lrspecs.toArray();
		}

		if (parentElement instanceof LRSpecDecorator) {
			List<LRSpecSubnodeDecorator> subnodes = new ArrayList<LRSpecSubnodeDecorator>();
			for (String readerName : ((LRSpecDecorator) parentElement)
					.getReaders().getReader()) {
				GetLRSpec spec = new GetLRSpec();
				spec.setName(readerName);
				try {
					subnodes.add(new LRSpecSubnodeDecorator(readerName,
							lrService.getLRSpec(spec),
							(LRSpecDecorator) parentElement, this));
				} catch (SecurityExceptionResponse e) {
					logger.fatal(e);
				} catch (ImplementationExceptionResponse e) {
					logger.fatal(e);
				} catch (NoSuchNameExceptionResponse e) {
					logger.warn(e);
				}
			}
			return subnodes.toArray();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object
	 * )
	 */
	@Override
	public Object getParent(Object element) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.
	 * Object)
	 */
	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof ALELRServicePortType) {
			return true;
		}
		if (element instanceof LRSpecDecorator) {
			return ((LRSpecDecorator) element).isIsComposite();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java
	 * .lang.Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void deleteReader(LRSpecDecorator reader) {
		Undefine undefine = new Undefine();
		undefine.setName(reader.getName());
		try {
			lrService.undefine(undefine);
		} catch (SecurityExceptionResponse e) {
			logger.fatal(e);
		} catch (InUseExceptionResponse e) {
			MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR
					| SWT.OK);
			messageBox.setMessage(e.toString());
			messageBox.open();
		} catch (ImplementationExceptionResponse e) {
			logger.fatal(e);
		} catch (ImmutableReaderExceptionResponse e) {
			MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR
					| SWT.OK);
			messageBox.setMessage(e.toString());
			messageBox.open();
		} catch (NoSuchNameExceptionResponse e) {
			MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR
					| SWT.OK);
			messageBox.setMessage(e.toString());
			messageBox.open();

		}
		updateReaderList(getReaderList());
	}

	/**
	 * Create a new reader.
	 * 
	 * @param name
	 * @param readerNames
	 */
	public void createReader(String name, List<String> readerNames) {
		Define define = new Define();
		define.setName(name);
		LRSpec spec = new LRSpec();
		spec.setIsComposite(true);
		LRSpec.Readers readers = new LRSpec.Readers();
		readers.getReader().addAll(readerNames);
		spec.setReaders(readers);
		define.setSpec(spec);
		try {
			lrService.define(define);
		} catch (SecurityExceptionResponse e) {
			logger.fatal(e);
		} catch (ImplementationExceptionResponse e) {
			logger.fatal(e);
		} catch (DuplicateNameExceptionResponse e) {
			MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR
					| SWT.OK);
			messageBox.setMessage(e.toString());
			messageBox.open();
		} catch (ValidationExceptionResponse e) {
			MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR
					| SWT.OK);
			messageBox.setMessage(e.toString());
			messageBox.open();
		}
		updateReaderList(getReaderList());
	}

	private void updateReaderList(List<LRSpecDecorator> newList) {
		lrspecs.clear();
		lrspecs.addAll(newList);
		viewer.setInput(viewer.getInput());
	}

	private List<LRSpecDecorator> getReaderList() {
		List<LRSpecDecorator> lrspecs = new ArrayList<LRSpecDecorator>();
		try {
			for (String name : lrService
					.getLogicalReaderNames(new EmptyParms()).getString()) {
				GetLRSpec spec = new GetLRSpec();
				spec.setName(name);
				// we need the reader names, decorate the reader
				lrspecs.add(new LRSpecDecorator(name,
						lrService.getLRSpec(spec), this));
			}
			return lrspecs;
		} catch (SecurityExceptionResponse e) {
			logger.fatal(e);
		} catch (ImplementationExceptionResponse e) {
			logger.fatal(e);
		} catch (NoSuchNameExceptionResponse e) {
			logger.warn(e);
		}
		return lrspecs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface
	 * .viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		lrspecs = new ArrayList<LRSpecDecorator>();
		if (newInput == null) {
			this.lrService = null;
			return;
		}
		if (!(newInput == null) && !(newInput instanceof ALELRServicePortType)) {
			throw new RuntimeException("Expected ALELRServicePortTypeImpl got "
					+ newInput.getClass());
		}
		this.lrService = (ALELRServicePortType) newInput;
		this.viewer = (TreeViewer) viewer;
		lrspecs.addAll(getReaderList());
		viewer.refresh();
	}

}