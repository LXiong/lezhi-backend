package com.buzzinate.common.dao;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.buzzinate.common.model.Similarity;
import com.buzzinate.common.sr.Neighborhood;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;

public class SimilarityDao extends BaseDaoDefault<Similarity, ObjectId> {

	public SimilarityDao(Datastore ds) {
		super(ds);
	}

	public Similarity findOne(long user1Id, long user2Id) {
		return findOne(createQuery().filter("user1Id", user1Id).filter("user2Id", user2Id));
	}

	public void insertOrUpdate(Similarity sim) {
		Similarity oldSim = findOne(sim.getUser1Id(), sim.getUser2Id());
		if (oldSim == null) {
			sim.setCreateAt(System.currentTimeMillis());
			save(sim);
		} else {
			oldSim.setIntersectSize(sim.getIntersectSize());
			oldSim.setSimilarity(sim.getSimilarity());
			oldSim.setHot(sim.isHot());
			save(oldSim);
		}
	}
	
	public List<Neighborhood> getHotNeighborhood(long userId) {
		List<Similarity> simes = createQuery().filter("user1Id", userId).filter("hot", true).asList();
		List<Neighborhood> neighborhood = new ArrayList<Neighborhood>(simes.size());
		
		for (int i = 0; i < simes.size(); i++) {
			Similarity sim = simes.get(i);
			neighborhood.add(new Neighborhood(sim.getUser2Id(), sim.getSimilarity()));
		}
		return neighborhood;
	}

	public int countNeighborhood(long userId) {
		return (int) count(createQuery().filter("user1Id", userId));
	}

	public void clearHot(long userId) {
		Query<Similarity> q = createQuery().filter("user1Id", userId);
		UpdateOperations<Similarity> uo = createUpdateOperations().set("hot", false);
		update(q, uo);
	}
}