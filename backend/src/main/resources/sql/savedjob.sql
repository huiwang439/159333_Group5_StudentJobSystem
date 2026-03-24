INSERT INTO users (
    account_status,
    created_at,
    email,
    full_name,
    password_hash,
    phone,
    role,
    updated_at
) VALUES (
    'ACTIVE',
    NOW(),
    'student@test.com',
    'Student User',
    '123456',
    '0210000000',
    'student',
    NOW()
);
INSERT INTO users (
    account_status,
    created_at,
    email,
    full_name,
    password_hash,
    phone,
    role,
    updated_at
) VALUES (
    'ACTIVE',
    NOW(),
    'employer@test.com',
    'Employer User',
    '123456',
    '0211111111',
    'employer',
    NOW()
);
INSERT INTO student_profiles (
    bio,
    created_at,
    degree_level,
    graduation_year,
    major,
    preferred_job_type,
    preferred_location,
    skills,
    university,
    updated_at,
    user_id
) VALUES (
    'I am a computer science student.',
    NOW(),
    'Bachelor',
    2026,
    'Computer Science',
    'Internship',
    'Auckland',
    'Java, Spring Boot, SQL',
    'Test University',
    NOW(),
    1
);
INSERT INTO jobs (
    category_id,
    created_at,
    deadline,
    description,
    employer_id,
    employment_type,
    field_of_study,
    location,
    requirements,
    salary_max,
    salary_min,
    status,
    title,
    updated_at,
    work_mode
) VALUES (
    NULL,
    NOW(),
    DATE_ADD(NOW(), INTERVAL 30 DAY),
    'Develop backend features for the job board system.',
    2,
    'Internship',
    'Computer Science',
    'Auckland',
    'Java, Spring Boot, MySQL',
    5000.00,
    3000.00,
    'OPEN',
    'Software Engineer Intern',
    NOW(),
    'Remote'
);

INSERT INTO saved_jobs (
    job_id,
    saved_at,
    student_profile_id
) VALUES (
    1,
    NOW(),
    1
);