    学习笔记

    基于电商交易场景（用户、商品、订单），设计一套简单的表结构，提 交DDL的SQL文件到Github（后面2周的作业依然要是用到这个表结构）。
    -- ----------------------------
    -- Table structure for order
    -- ----------------------------
    DROP TABLE IF EXISTS `order`;
    CREATE TABLE `order`  (
      `order_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
      `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户id',
      `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '配送地址',
      `amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '订单金额',
      `status` int(2) NULL DEFAULT NULL COMMENT '订单状态： 1未支付 2.已支付 3.已发货 4.正在配送 5.已签收 6 交易成功 7 交易关闭',
      `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
      `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
      `comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
      PRIMARY KEY (`order_id`) USING BTREE
    ) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

    -- ----------------------------
    -- Records of order
    -- ----------------------------

    -- ----------------------------
    -- Table structure for order_item
    -- ----------------------------
    DROP TABLE IF EXISTS `order_item`;
    CREATE TABLE `order_item`  (
      `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
      `order_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '所属订单id',
      `product_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品id',
      `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品名称',
      `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '价格',
      `quantity` int(10) NULL DEFAULT NULL COMMENT '数量',
      `amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '总金额',
      `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
      `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
      PRIMARY KEY (`id`) USING BTREE
    ) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

    -- ----------------------------
    -- Records of order_item
    -- ----------------------------

    -- ----------------------------
    -- Table structure for product
    -- ----------------------------
    DROP TABLE IF EXISTS `product`;
    CREATE TABLE `product`  (
      `product_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
      `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '商品价格',
      `product_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品名称',
      `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
      `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
      `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品描述',
      `product_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '所属品类',
      `status` int(2) NULL DEFAULT NULL COMMENT '状态 1：在售 2 下架'
    ) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

    -- ----------------------------
    -- Records of product
    -- ----------------------------

    -- ----------------------------
    -- Table structure for user
    -- ----------------------------
    DROP TABLE IF EXISTS `user`;
    CREATE TABLE `user`  (
      `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
      `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '昵称',
      `sex` int(2) NULL DEFAULT NULL COMMENT '性别：1 男 2 女',
      `birth_date` bigint(15) NULL DEFAULT NULL COMMENT '出生日期',
      `real_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '真实姓名',
      `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
      `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
      PRIMARY KEY (`user_id`) USING BTREE
    ) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

    -- ----------------------------
    -- Records of user
    -- ----------------------------

    SET FOREIGN_KEY_CHECKS = 1;
