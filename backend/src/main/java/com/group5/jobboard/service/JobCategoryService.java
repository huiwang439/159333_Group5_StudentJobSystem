package com.group5.jobboard.service;

import java.util.List;
import java.util.Map;

public interface JobCategoryService {

    Map<String, Object> createCategory(String name);

    List<Map<String, Object>> getAllCategories();

    Map<String, Object> deleteCategory(Long categoryId);
}