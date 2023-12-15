/**
 *  Copyright (C) 2002-2015   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.common.option;

import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import net.sf.freecol.common.io.FreeColXMLReader;
import net.sf.freecol.common.model.FreeColObject;
import net.sf.freecol.common.model.Specification;


/**
 * The super class of all options.  GUI components making use of this
 * class can refer to its name and shortDescription properties.  The
 * complete keys of these properties consist of the identifier of the
 * option group (if any), followed by a "."  unless the option group
 * is null, followed by the identifier of the option object, followed
 * by a ".", followed by "name" or "shortDescription".
 */
public abstract class AbstractOption<T> extends FreeColObject
    implements Option<T> {

    private static final Logger logger = Logger.getLogger(AbstractOption.class.getName());

    /** The option group prefix. */
    private String optionGroup = "";

    /**
     * Determine if the option has been defined.  When defined an
     * option won't change when a default value is read from an XML file.
     */
    protected boolean isDefined = false;


    /**
     * Creates a new <code>AbstractOption</code>.
     *
     * @param id The object identifier.
     */
    public AbstractOption(String id) {
        setId(id);
    }

    /**
     * Creates a new <code>AbstractOption</code>.
     *
     * @param specification The <code>Specification</code> to refer to.
     */
    public AbstractOption(Specification specification) {
        this(null, specification);
    }

    /**
     * Creates a new <code>AbstractOption</code>.
     *
     * @param id The object identifier.
     * @param specification The <code>Specification</code> to refer to.
     */
    public AbstractOption(String id, Specification specification) {
        setId(id);
        setSpecification(specification);
    }


    /**
     * Gets the string prefix that identifies the group of this
     * <code>Option</code>.
     *
     * @return The string prefix provided by the OptionGroup.
     */
    public String getGroup() {
        return optionGroup;
    }

    /**
     * Set the option group prefix.
     *
     * @param group The prefix to set.
     */
    public void setGroup(String group) {
        optionGroup = (group == null) ? "" : group;
    }

    /**
     * Sets the values from another option.
     *
     * @param source The other <code>AbstractOption</code>.
     */
    protected void setValues(AbstractOption<T> source) {
        setId(source.getId());
        setSpecification(source.getSpecification());
        setValue(source.getValue());
        setGroup(source.getGroup());
        isDefined = source.isDefined;
    }

    /**
     * Sets the value of this option from the given string
     * representation.  Both parameters must not be null at the same
     * time.  This method does nothing.  Override it if the option has
     * a suitable string representation.
     *
     * @param valueString The string representation of the value of
     *     this <code>Option</code>.
     * @param defaultValueString The string representation of the
     *     default value of this <code>Option</code>.
     * @exception XMLStreamException if the value is invalid.
     */
    protected void setValue(String valueString, String defaultValueString)
        throws XMLStreamException {
        throw new XMLStreamException("Unsupported method: setValue.");
    }

    /**
     * Generate the choices to provide to the UI.
     *
     * Override if the subclass needs to determine its choices dynamically.
     */
    public void generateChoices() {
        // do nothing
    }

    /**
     * Is null an acceptable value for this option?
     *
     * Override this in subclasses where necessary.
     *
     * @return False.
     */
    public boolean isNullValueOK() {
        return false;
    }


    // Interface Option

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract AbstractOption<T> clone() throws CloneNotSupportedException;

    /**
     * Gets the value of this option.
     *
     * @return The value of this <code>Option</code>.
     */
    @Override
    public abstract T getValue();

    /**
     * Sets the value of this option.
     *
     * @param value The new value of this <code>Option</code>.
     */
    @Override
    public abstract void setValue(T value);


    // Serialization

    protected static final String ACTION_TAG = "action";
    protected static final String DEFAULT_VALUE_TAG = "defaultValue";


    /**
     * {@inheritDoc}
     */
    @Override
    protected void readAttributes(FreeColXMLReader xr) throws XMLStreamException {
        super.readAttributes(xr);

        String defaultValue = xr.getAttribute(DEFAULT_VALUE_TAG, (String)null);

        String value = xr.getAttribute(VALUE_TAG, (String)null);
        if (defaultValue == null && value == null) {
            if (!isNullValueOK()) {
                throw new XMLStreamException("invalid option " + getId()
                    + ": no value nor default value found.");
            }
        } else {
            setValue(value, defaultValue);
        }
    }

    // Note: writeAttributes() is not needed/present.
    // - The identifier is correctly written by the super class.
    // - The default value does not need to be written in general.
    // - The value *must* be written by the implementing subclass.

/**
 * General option reader routine.
 *
 * @param xr The <code>FreeColXMLReader</code> to read from.
 * @return An option.
 */
protected AbstractOption readOption(FreeColXMLReader xr) throws XMLStreamException {
    final Specification spec = getSpecification();
    final String tag = xr.getLocalName();
    AbstractOption option = null;

    if (ACTION_TAG.equals(tag)) {
        skipAction(xr);

    } else {
        option = createOptionByTag(tag, spec);
    }

    if (option != null) option.readFromXML(xr);
    return option;
}

/**
 * Skips the action tag.
 *
 * @param xr The <code>FreeColXMLReader</code> to read from.
 * @throws XMLStreamException if there is an error reading the XML stream.
 */
private void skipAction(FreeColXMLReader xr) throws XMLStreamException {
    // FIXME: load FreeColActions from client options?
    logger.finest("Skipping action " + xr.readId());
    xr.nextTag();
}

/**
 * Creates an option based on the given tag and specification.
 *
 * @param tag  The XML element tag.
 * @param spec The specification.
 * @return The created option.
 */
private AbstractOption createOptionByTag(String tag, Specification spec) {
    switch (tag) {
        case AbstractUnitOption.getXMLElementTagName():
            return new AbstractUnitOption(spec);
        case AudioMixerOption.getXMLElementTagName():
            return new AudioMixerOption(spec);
        case BooleanOption.getXMLElementTagName():
            return new BooleanOption(spec);
        case FileOption.getXMLElementTagName():
            return new FileOption(spec);
        case IntegerOption.getXMLElementTagName():
            return new IntegerOption(spec);
        case LanguageOption.getXMLElementTagName():
            return new LanguageOption(spec);
        case ModListOption.getXMLElementTagName():
            return new ModListOption(spec);
        case ModOption.getXMLElementTagName():
            return new ModOption(spec);
        case OptionGroup.getXMLElementTagName():
            return new OptionGroup(spec);
        case PercentageOption.getXMLElementTagName():
            return new PercentageOption(spec);
        case RangeOption.getXMLElementTagName():
            return new RangeOption(spec);
        case SelectOption.getXMLElementTagName():
            return new SelectOption(spec);
        case StringOption.getXMLElementTagName():
            return new StringOption(spec);
        case UnitListOption.getXMLElementTagName():
            return new UnitListOption(spec);
        case UnitTypeOption.getXMLElementTagName():
            return new UnitTypeOption(spec);
        case TextOption.getXMLElementTagName():
            return new TextOption(spec);
        default:
            logger.warning("Not an option type: " + tag);
            return null;
    }
}
