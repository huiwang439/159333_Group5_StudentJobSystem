package com.group5.jobboard.service.impl;

import com.group5.jobboard.entity.JobCategory;
import com.group5.jobboard.repository.JobCategoryRepository;
import com.group5.jobboard.service.JobCategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JobCategoryServiceImpl implements JobCategoryService {

    private final JobCategoryRepository jobCategoryRepository;

    public JobCategoryServiceImpl(JobCategoryRepository jobCategoryRepository) {
        this.jobCategoryRepository = jobCategoryRepository;
    }

    @Override
    public Map<String, Object> createCategory(String name) {
        jobCategoryRepository.findByCategoryName(name)
                .ifPresent(category -> {
                    throw new RuntimeException("Category already exists");
                });

        JobCategory category = new JobCategory();
        category.setCategoryName(name);

        jobCategoryRepository.save(category);

        Map<String, Object> result = new HashMap<>();
        result.put("categoryId", category.getId());
        result.put("categoryName", category.getCategoryName());
        result.put("categoryDescription", category.getCategoryDescription());

        return result;
    }

    @Override
    public List<Map<String, Object>> getAllCategories() {
        List<JobCategory> categories = jobCategoryRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (JobCategory category : categories) {
            Map<String, Object> item = new HashMap<>();
            item.put("categoryId", category.getId());
            item.put("categoryName", category.getCategoryName());
            item.put("categoryDescription", category.getCategoryDescription());
            result.add(item);
        }

        return result;
    }

    @Override
    public Map<String, Object> deleteCategory(Long categoryId) {
        JobCategory category = jobCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        jobCategoryRepository.delete(category);

        Map<String, Object> result = new HashMap<>();
        result.put("categoryId", category.getId());
        result.put("deleted", true);

        return result;
    }
}