-- 修正默认用户密码哈希（与注释中的明文密码保持一致）
UPDATE sys_user SET password = '$2b$10$2UxeWg80ZFa.s6xNGRTNt.cLmOeSFXROCc55pc3/HHrIb7Tivd/rC' WHERE username = 'admin';
UPDATE sys_user SET password = '$2b$10$pgkn9X3T6Ni2xlcrIE6/EerCKE3Idy3IeSiMwdO2he/8KmTtppGTy' WHERE username = 'librarian';
UPDATE sys_user SET password = '$2b$10$XSLJZiSmBtpdq74yPlbcBeeUS3.D/lVHz9YcjTZp52ZsDd/grupfW' WHERE username = 'reader01';
