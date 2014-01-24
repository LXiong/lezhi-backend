package com.buzzinate.jianghu.dao.mongo;

import com.buzzinate.jianghu.model.CoverStory.StoryType;
import com.google.code.morphia.converters.SimpleValueConverter;
import com.google.code.morphia.converters.TypeConverter;
import com.google.code.morphia.mapping.MappedField;
import com.google.code.morphia.mapping.MappingException;

public class StoryTypeConvert extends TypeConverter implements SimpleValueConverter {
    public StoryTypeConvert() {
        super(StoryType.class);
    }

    @Override
    protected boolean isSupported(Class<?> c, MappedField optionalExtraInfo) {
        return c.equals(StoryType.class);
    }

    @Override
    public Object decode(@SuppressWarnings("rawtypes") Class targetClass, Object fromDBObject,
                         MappedField optionalExtraInfo) throws MappingException {
        if (fromDBObject == null)
            return null;
        if (fromDBObject instanceof String) {
            return null;
        }
        if (fromDBObject instanceof Float) {
            return StoryType.fromInt(((Float) fromDBObject).intValue());
        }
        if (fromDBObject instanceof Long) {
            return StoryType.fromInt(((Long) fromDBObject).intValue());
        }
        try {
            return StoryType.fromInt(((Integer) fromDBObject).intValue());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Object encode(Object value, MappedField optionalExtraInfo) {
        if (value == null)
            return null;
        return Integer.valueOf(((StoryType) value).getCode());
    }

}