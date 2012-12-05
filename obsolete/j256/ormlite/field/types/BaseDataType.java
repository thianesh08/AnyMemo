package com.j256.ormlite.field.types;

import java.lang.reflect.Field;
import java.sql.SQLException;

import com.j256.ormlite.field.BaseFieldConverter;
import com.j256.ormlite.field.DataPersister;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Base data type that defines the defaults the various data types.
 * 
 * <p>
 * Here's a good page about the <a href="http://docs.codehaus.org/display/CASTOR/Type+Mapping" >mapping for a number of
 * database types</a>:
 * </p>
 * 
 * @author graywatson
 */
public abstract class BaseDataType extends BaseFieldConverter implements DataPersister {

	/**
	 * Type of the data as it is persisted in SQL-land. For example, if you are storing a DateTime, you might consider
	 * this to be a {@link SqlType#LONG} if you are storing it as epoche milliseconds.
	 */
	private final SqlType sqlType;
	private final Class<?>[] classes;

	/**
	 * @param sqlType
	 *            Type of the class as it is persisted in the databases.
	 * @param classes
	 *            Associated classes for this type. These should be specified if you want this type to be always used
	 *            for these Java classes. If this is a custom persister then this array should be empty.
	 */
	public BaseDataType(SqlType sqlType, Class<?>[] classes) {
		this.sqlType = sqlType;
		this.classes = classes;
	}

	public abstract Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException;

	public abstract Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos)
			throws SQLException;

	public boolean isValidForField(Field field) {
		if (classes.length == 0) {
			// we can't figure out the types so we just say it is valid
			return true;
		}
		for (Class<?> clazz : classes) {
			if (clazz.isAssignableFrom(field.getType())) {
				return true;
			}
		}
		// if classes are specified and one of them should match
		return false;
	}

	/**
	 * @throws SQLException
	 *             If there are problems creating the config object.
	 */
	public Object makeConfigObject(FieldType fieldType) throws SQLException {
		return null;
	}

	public SqlType getSqlType() {
		return sqlType;
	}

	public Class<?>[] getAssociatedClasses() {
		return classes;
	}

	public Object convertIdNumber(Number number) {
		// by default the type cannot convert an id number
		return null;
	}

	public boolean isValidGeneratedType() {
		return false;
	}

	public boolean isEscapedDefaultValue() {
		// default is to not escape the type if it is a number
		return isEscapedValue();
	}

	public boolean isEscapedValue() {
		return true;
	}

	public boolean isPrimitive() {
		return false;
	}

	public boolean isComparable() {
		return true;
	}

	public boolean isAppropriateId() {
		return true;
	}

	public boolean isArgumentHolderRequired() {
		return false;
	}

	public boolean isSelfGeneratedId() {
		return false;
	}

	public Object generateId() {
		throw new IllegalStateException("Should not have tried to generate this type");
	}

	public int getDefaultWidth() {
		return 0;
	}

	public boolean dataIsEqual(Object fieldObj1, Object fieldObj2) {
		if (fieldObj1 == null) {
			return (fieldObj2 == null);
		} else if (fieldObj2 == null) {
			return false;
		} else {
			return fieldObj1.equals(fieldObj2);
		}
	}

	public boolean isValidForVersion() {
		return false;
	}

	public Object moveToNextValue(Object currentValue) {
		return null;
	}
}
