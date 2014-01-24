package com.buzzinate.common.dao;

import com.buzzinate.common.model.Vocabulary;
import com.google.code.morphia.Datastore;

public class VocabularyDao extends BaseDaoDefault<Vocabulary, String> implements BaseDao<Vocabulary, String> {

	public VocabularyDao(Datastore ds) {
		super(ds);
	}
}