/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.basic;

import java.io.IOException;
import java.util.ArrayList;

import org.openlowcode.design.action.DynamicActionDefinition;
import org.openlowcode.design.data.ArgumentContent;
import org.openlowcode.design.data.DataAccessMethod;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.Index;
import org.openlowcode.design.data.MethodAdditionalProcessing;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.ObjectIdStoredElement;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.StoredElement;
import org.openlowcode.design.data.argument.ArrayArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;
import org.openlowcode.tools.misc.NamedList;

/**
 * This property allows unique storage and retrieval independently from the
 * database by giving it a unique persistent technical id. It is necessary for
 * most other properties.
 * 
 * * <br>
 * Dependent property :
 * {@link org.openlowcode.design.data.properties.basic.StoredObject}
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class UniqueIdentified
		extends
		Property<UniqueIdentified> {
	private NamedList<DynamicActionDefinition> actionsonobjectid;
	private NamedList<DynamicActionDefinition> actionsonobjectidonmanagetab;
	private StoredObject storedobject;

	/**
	 * @return the list of actions on object id to be added to button band of the
	 *         object page
	 */
	public NamedList<DynamicActionDefinition> getActionListonObjectId() {
		return actionsonobjectid;
	}

	/**
	 * @return the list of actions on object id to be added to the manage tab of the
	 *         object page
	 */
	public NamedList<DynamicActionDefinition> getActionListonObjectIdForManageTab() {
		return actionsonobjectidonmanagetab;
	}

	/**
	 * @return get the related property stored object
	 */
	public StoredObject getStoredObject() {
		return this.storedobject;
	}

	/**
	 * creates the unique identified property
	 */
	public UniqueIdentified() {
		super("UNIQUEIDENTIFIED");
		this.actionsonobjectid = new NamedList<DynamicActionDefinition>();
		this.actionsonobjectidonmanagetab = new NamedList<DynamicActionDefinition>();

	}

	@Override
	public String[] getPropertyDeepCopyStatement() {

		return null;
	}

	@Override
	public void controlAfterParentDefinition() {
		this.storedobject = (StoredObject) parent.getPropertyByName("STOREDOBJECT");
		if (this.storedobject == null)
			throw new RuntimeException("UniqueIdentified is dependent on storedobject");
		this.addDependentProperty(storedobject);
		MethodAdditionalProcessing insertidgeneration = new MethodAdditionalProcessing(true,
				storedobject.getDataAccessMethod("INSERT"));
		this.addMethodAdditionalProcessing(insertidgeneration);
		DataAccessMethod read = new DataAccessMethod("READONE", new ObjectArgument("OBJECT", parent), false);
		read.addInputArgument(new MethodArgument("ID", new ObjectIdArgument("ID", parent)));
		this.addDataAccessMethod(read);

		DataAccessMethod readseveral = new DataAccessMethod("READSEVERAL",
				new ArrayArgument(new ObjectArgument("OBJECT", parent)), false);
		readseveral.addInputArgument(new MethodArgument("ID", new ArrayArgument(new ObjectIdArgument("ID", parent))));
		this.addDataAccessMethod(readseveral);

		// get Id

		// DELETE
		DataAccessMethod delete = new DataAccessMethod("DELETE", null, false, true);
		delete.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(delete);

		// UPDATE
		DataAccessMethod update = new DataAccessMethod("UPDATE", null, false, true);
		update.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(update);

		// REFRESH
		DataAccessMethod refresh = new DataAccessMethod("REFRESH", null, false);
		refresh.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(refresh);

		// Field

		StoredElement id = new ObjectIdStoredElement("ID", parent);
		this.addElement(id, "Id", "technical identification", Property.FIELDDISPLAY_NORMAL, -50, 25);
		this.addIndex(new Index("ID", id, true));
	}

	@Override
	public String getJavaType() {
		return "#NOT IMPLEMENTED#";
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		ArrayList<DataObjectDefinition> dependencies = new ArrayList<DataObjectDefinition>();
		return dependencies;
	}

	/**
	 * adds action in the main button band of the object. The action should have a
	 * single input attribute being the data object id
	 * 
	 * @param action adds an action on the object id. The action should have a
	 *               single input attribute being the data object id
	 */
	public void addActionOnObjectId(DynamicActionDefinition action) {
		addActionOnObjectId(action, false);
	}

	/**
	 * adds an action on the object, either in the main button band, or in the
	 * manage tab
	 * 
	 * @param action            adds an action on the object id. The action should
	 *                          have a single input attribute being the data object
	 *                          id
	 * @param actioninmanagetab if true, action is put in manage tabs, if false,
	 *                          action is directly in the action button
	 */
	public void addActionOnObjectId(DynamicActionDefinition action, boolean actioninmanagetab) {
		if (action.getInputArguments().getSize() != 1)
			throw new RuntimeException("you can add an action on object id only if it has 1 argument, action "
					+ action.getName() + " has " + action.getInputArguments().getSize() + ".");
		ArgumentContent uniqueinputargument = action.getInputArguments().get(0);
		if (!(uniqueinputargument instanceof ObjectIdArgument))
			throw new RuntimeException("the first argument of " + action.getName()
					+ " should be ObjectidArgument, it is actually " + uniqueinputargument.getClass().getName() + ".");
		ObjectIdArgument objectidargument = (ObjectIdArgument) uniqueinputargument;
		DataObjectDefinition objectforid = objectidargument.getObject();
		if (objectforid != parent) {
			throw new RuntimeException("objectid should be of consistent type, actionid type = "
					+ objectforid.getOwnermodule().getName() + "/" + objectforid.getName() + ", object parentid type = "
					+ parent.getOwnermodule().getName() + "/" + parent.getName());
		}

		if (actioninmanagetab) {
			this.actionsonobjectidonmanagetab.add(action);
		} else {
			actionsonobjectid.add(action);

		}
	}

	@Override
	public String[] getPropertyInitMethod() {
		return new String[0];
	}

	@Override
	public void setFinalSettings() {
	}

	@Override
	public String[] getPropertyExtractMethod() {
		return new String[0];
	}

}
