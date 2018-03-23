package com.technocomp.ems.dao;

import com.technocomp.ems.model.User;

public interface IUserInfoDAO {
	User getActiveUser(String userName);
}