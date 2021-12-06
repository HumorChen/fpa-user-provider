package cn.freeprogramming.service.impl;

import cn.freeprogramming.bean.User;
import cn.freeprogramming.mapper.UserMapper;
import cn.freeprogramming.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author humorchen
 * @since 2021-12-05
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
