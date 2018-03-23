package com.technocomp.ems.service;

import com.technocomp.ems.model.Article;
import java.util.List;

import org.springframework.security.access.annotation.Secured;

public interface IArticleService {
	 @Secured ({"ROLE_ADMIN", "ROLE_USER"})
     List<Article> getAllArticles();
	 @Secured ({"ROLE_ADMIN", "ROLE_USER"})
     Article getArticleById(int articleId);
	 @Secured ({"ROLE_ADMIN"})
     boolean addArticle(Article article);
	 @Secured ({"ROLE_ADMIN"})
     void updateArticle(Article article);
	 @Secured ({"ROLE_ADMIN"})
     void deleteArticle(int articleId);
}
