package cn.freeprogramming.facade.impl;

import cn.freeprogramming.bean.User;
import cn.freeprogramming.enums.UserErrorEnums;
import cn.freeprogramming.facade.IUserFacade;
import cn.freeprogramming.params.LoginParam;
import cn.freeprogramming.params.ModifySelfInfoParam;
import cn.freeprogramming.params.RegisterParam;
import cn.freeprogramming.params.RetrieveParam;
import cn.freeprogramming.service.IUserService;
import cn.freeprogramming.util.BusinessAssert;
import cn.freeprogramming.utils.JwtUtils;
import cn.freeprogramming.utils.PasswordUtils;
import cn.freeprogramming.vo.result.R;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * @author humorchen chenxingxing
 * @date 2021/12/5 22:56 2021/12/11 19:56
 *
 */
// TODO 用户服务调用基本操作的service实现
@DubboService
@Slf4j
public class IUserFacadeImpl implements IUserFacade {

    @Autowired
    private IUserService userService;


    /**
     * 登录
     *
     * @param loginParam
     * @return
     */
    @Override
    public R login(LoginParam loginParam, String lastOnlineIp) {


        String phone = loginParam.getPhone();
        String password = loginParam.getPassword();

        //看用户表里有没有该手机号
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone);
        User user = userService.getOne(wrapper);
        BusinessAssert.isTrue(user != null,UserErrorEnums.USER_NOT_FOUND);

        //验证密码是否正确
        boolean flag = PasswordUtils.checkPasswordHash(password, user.getPassword());
        BusinessAssert.isTrue(flag,UserErrorEnums.LOGIN_FAILED);

        //验证该用户是否被禁用
        Integer state = user.getState();
        BusinessAssert.isTrue(state != 0,UserErrorEnums.USER_IS_BAN);

        Integer userId = user.getId();
        String token = JwtUtils.createToken(userId);

        //设置登录时间和登录ip
        user.setLastOnlineTime(LocalDateTime.now());
        user.setLastOnlineIp(lastOnlineIp);
        userService.updateById(user);

        return R.success("登录成功",token);
    }

    /**
     * 注册
     *
     * @param registerParam
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public R register(RegisterParam registerParam) {

        //校验手机号
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        String phone = registerParam.getPhone();
        queryWrapper.eq(User.PHONE,phone);

        int count = userService.count();
        BusinessAssert.isTrue(count > 0, UserErrorEnums.USER_IS_EXIST);

        //新建User
        User user = new User();

        //填充User
        user.setPhone(phone);

        String password = registerParam.getPassword();
        String nickname = registerParam.getNickname();
        user.setPassword(PasswordUtils.encode(PasswordUtils.getSalt(),password));
        user.setNickname(nickname);
        user.setRegisterTime(LocalDateTime.now());
        userService.save(user);

        return R.success("注册成功");
    }

    /**
     * 找回密码
     *
     * @param retrieveParam
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public R retrieve(RetrieveParam retrieveParam) {


        //通过用户id查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(User.ID,retrieveParam.getUserId());
        User user = userService.getOne(queryWrapper);
        //对密码进行校验
        String oldPassword = retrieveParam.getOldPassword();
        boolean flag = PasswordUtils.checkPasswordHash(oldPassword, user.getPassword());
        BusinessAssert.isTrue(flag,UserErrorEnums.LOGIN_FAILED);

        //验证该用户是否被禁用
        Integer state = user.getState();
        BusinessAssert.isTrue(state != 0,UserErrorEnums.USER_IS_BAN);

        //6.修改密码
        String newPassword = retrieveParam.getNewPassword();
        newPassword = PasswordUtils.encode(newPassword);
        user.setPassword(newPassword);

        BusinessAssert.isTrue(userService.updateById(user), UserErrorEnums.UPDATE_PASSWORD_FAILED);
        //7.修改成功，返回
        return R.success("修改密码成功");

    }

    /**
     * 修改个人信息
     *
     * @param modifySelfInfoParam
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public R modifySelfInfo(ModifySelfInfoParam modifySelfInfoParam) {

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        User user = new User();

        user.setSign(modifySelfInfoParam.getSign());
        user.setPhone(modifySelfInfoParam.getPhone());
        user.setNickname(modifySelfInfoParam.getNickname());
        user.setAddress(modifySelfInfoParam.getAddress());
        user.setBirthday(modifySelfInfoParam.getBirthday());
        user.setEmail(modifySelfInfoParam.getEmail());
        user.setGender(modifySelfInfoParam.getGender());
        user.setIdcard(modifySelfInfoParam.getIdcard());
        user.setQq(modifySelfInfoParam.getQq());
        user.setRealName(modifySelfInfoParam.getRealName());
        user.setWechat(modifySelfInfoParam.getWechat());
        user.setSchoolName(modifySelfInfoParam.getSchoolName());

        userService.update(user,queryWrapper.eq(User.PHONE,user.getPhone()));

        return null;
    }
}
