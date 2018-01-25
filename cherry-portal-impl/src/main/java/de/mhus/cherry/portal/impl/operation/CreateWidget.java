package de.mhus.cherry.portal.impl.operation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import com.vaadin.client.WidgetUtil;

import aQute.bnd.annotation.component.Component;
import de.mhus.cherry.portal.api.CallContext;
import de.mhus.cherry.portal.api.CherryApi;
import de.mhus.cherry.portal.api.VirtualHost;
import de.mhus.cherry.portal.api.WidgetApi;
import de.mhus.cherry.portal.api.control.EditorFactory;
import de.mhus.cherry.portal.api.util.CherryUtil;
import de.mhus.lib.cao.CaoNode;
import de.mhus.lib.cao.aspect.StructureControl;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.strategy.NotSuccessful;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.strategy.OperationDescription;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.strategy.Successful;
import de.mhus.lib.core.strategy.TaskContext;
import de.mhus.lib.form.DataSource;
import de.mhus.lib.form.Item;
import de.mhus.lib.form.PropertiesDataSource;
import de.mhus.lib.form.definition.FmCheckbox;
import de.mhus.lib.form.definition.FmCombobox;
import de.mhus.lib.form.definition.FmText;
import de.mhus.lib.vaadin.operation.AbstractVaadinOperation;
import de.mhus.lib.vaadin.operation.AbstractVaadinOperationEditor;
import de.mhus.lib.vaadin.operation.AbstractVaadinOperationForm;
import de.mhus.osgi.sop.api.Sop;

@Component(properties="tags=control|caonode|create",provide=Operation.class)
public class CreateWidget extends AbstractVaadinOperation {

	@Override
	public boolean canExecute(TaskContext context) {
		if (!super.canExecute(context)) return false;
		
		CaoNode[] navArray = CherryUtil.getNodeFromProperties(context.getParameters());
		if (navArray == null || navArray.length < 1) return false; 
		CaoNode nav = navArray[0];
		return CherryUtil.isPageNode(null, nav) || CherryUtil.isPageNode(null, nav.getParent());
	}
	
	@Override
	protected AbstractVaadinOperationEditor createEditor() {
		return new AbstractVaadinOperationForm(this) {
			
			@Override
			protected void initDataSource(PropertiesDataSource ds) {
				MProperties properties = new MProperties();
				ds.setProperties(properties);
				
				// Page Items
				VirtualHost vHost = MApi.lookup(CherryApi.class).getCurrentCall().getVirtualHost();
				// Page Type Items
				{
					CaoNode[] navArray = CherryUtil.getNodeFromProperties(editorProperties);
					CaoNode nav = navArray[0];
					
					if (!CherryUtil.isPageNode(null, nav)) nav = nav.getParent();
					
					CallContext call = MApi.lookup(CherryApi.class).getCurrentCall();
					Collection<EditorFactory> list = call.getVirtualHost().getAvailableWidgetTypes(nav);
					LinkedList<Item> pageTypeTypes = new LinkedList<>();
					for (EditorFactory editor : list) {
						pageTypeTypes.add(new Item(editor.getIdent(), editor.getCaption() ));
					}
	
					Item[] pageTypeTypesArray = pageTypeTypes.toArray(new Item[pageTypeTypes.size()]);
					properties.put("widget." + DataSource.ITEMS, pageTypeTypesArray);
					properties.setBoolean("hidden", false);
				}	

			}
			
		};
	}

	@Override
	protected OperationResult doExecute2(TaskContext context) throws Exception {
		
		// data
		CaoNode[] navArray = CherryUtil.getNodeFromProperties(context.getParameters());
		CaoNode nav = navArray[0];
		String title = context.getParameters().getString("title");
		String name = context.getParameters().getString("name", MFile.normalize(title));
		boolean hidden = context.getParameters().getBoolean("hidden", true);
		String type = context.getParameters().getString("widget");
		
		VirtualHost vHost = MApi.lookup(CherryApi.class).getCurrentCall().getVirtualHost();
		
		// create page node
		CaoNode newWidget = null;
		{
			if (!CherryUtil.isPageNode(null, nav)) nav = nav.getParent();

			StructureControl control = nav.adaptTo(StructureControl.class);
			MProperties properties = new MProperties();
			properties.setString(WidgetApi.RES_TITLE, title);
			properties.setString(WidgetApi.RENDERER, type);
			
			newWidget = control.createChildNode(name, properties);
			if (newWidget == null) return new NotSuccessful(this, "not created", "error=Can't create node", -1);
			
//				StructureControl controlNew = newPage.adaptTo(StructureControl.class);
//				controlNew.moveToBottom();
			vHost.doPrepareCreatedWidget(newWidget);
		}
		
		return new Successful(this, "ok", newWidget);
	}

	@Override
	protected OperationDescription createDescription() {
		return new OperationDescription(this, getCaption(), new DefRoot(
					new FmCombobox("widget", "Widget", "Widget"),
					new FmText("title", "Page Title", "Title of the new page"),
					new FmText("name", "Node Name", "Technical node name shown in path, leave blank for default"),
					new FmCheckbox("hidden", "Hidden", "Set node to hidden")
				));
	}

	protected String getCaption() {
		return "Create Widget";
	}

}
