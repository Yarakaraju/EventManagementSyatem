package com.technocomp.ems.dao;
import com.technocomp.ems.model.User;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class UserInfoDAO implements IUserInfoDAO {
	@PersistenceContext	
	private EntityManager entityManager;
	public User getActiveUser(String userName) {
		User activeUserInfo = new User();
		short enabled = 1;
		List<?> list = entityManager.createQuery("SELECT u FROM User u WHERE mobile=? and active=?")
				.setParameter(1, userName).setParameter(2, enabled).getResultList();
		if(!list.isEmpty()) {
			activeUserInfo = (User)list.get(0);
		}
		return activeUserInfo;
	}
}