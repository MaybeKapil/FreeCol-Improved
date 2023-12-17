/**
 * Copyright (C) 2002-2015   The FreeCol Team This file is part of FreeCol. FreeCol is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version. FreeCol is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.freecol.common.model;


import java.util.List;
import net.sf.freecol.common.util.RandomChoice;
import java.util.Collections;
import java.util.ArrayList;

public class TileTypeProduct {
	private List<RandomChoice<ResourceType>> resourceTypes = null;

	public void setResourceTypes(List<RandomChoice<ResourceType>> resourceTypes) {
		this.resourceTypes = resourceTypes;
	}

	/**
	* Gets the resources that can be placed on this tile type.
	* @return  A weighted list of resource types.
	*/
	public List<RandomChoice<ResourceType>> getWeightedResources() {
		return (resourceTypes == null) ? Collections.<RandomChoice<ResourceType>>emptyList() : resourceTypes;
	}

	/**
	* Add a resource type.
	* @param type  The <code>ResourceType</code> to add.
	* @param prob  The percentage probability of the resource being present.
	*/
	public void addResourceType(ResourceType type, int prob) {
		if (resourceTypes == null)
			resourceTypes = new ArrayList<>();
		resourceTypes.add(new RandomChoice<>(type, prob));
	}

	/**
	* Gets the resource types that can be found on this tile type.
	* @return  A list of <code>ResourceType</code>s.
	*/
	public List<ResourceType> getResourceTypes() {
		List<ResourceType> result = new ArrayList<>();
		if (resourceTypes != null) {
			for (RandomChoice<ResourceType> resource : resourceTypes) {
				result.add(resource.getObject());
			}
		}
		return result;
	}
}