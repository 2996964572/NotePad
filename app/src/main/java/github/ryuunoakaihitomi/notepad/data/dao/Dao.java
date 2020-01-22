package github.ryuunoakaihitomi.notepad.data.dao;

import java.util.List;

interface Dao<T> {

    /**
     * 插入数据
     *
     * @param entity 数据实体
     * @return 身份标识
     */
    long insert(T entity);

    /**
     * 返回所有数据
     *
     * @return 数据实体列表
     */
    List<T> findAll();

    /**
     * 根据身份标识查找数据
     *
     * @param id 身份标识
     * @return 具有指定身份标识的数据实体
     */
    T findById(long id);

    /**
     * 更新数据
     *
     * @param id     待更新旧数据的身份标识
     * @param entity 新的数据实体
     */
    void update(long id, T entity);

    /**
     * 删除指定的数据
     *
     * @param id 身份标识
     */
    void delete(long id);
}
