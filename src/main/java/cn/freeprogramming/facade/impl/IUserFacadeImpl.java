package cn.freeprogramming.facade.impl;

import cn.freeprogramming.enums.CommonErrorEnums;
import cn.freeprogramming.bean.User;
import cn.freeprogramming.enums.ErrorEnums;
import cn.freeprogramming.facade.IUserFacade;
import cn.freeprogramming.params.LoginParam;
import cn.freeprogramming.params.ModifySelfInfoParam;
import cn.freeprogramming.params.RegisterParam;
import cn.freeprogramming.params.RetrieveParam;
import cn.freeprogramming.service.IUserService;
import cn.freeprogramming.util.BusinessAssert;
import cn.freeprogramming.vo.result.R;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author humorchen
 * @date 2021/12/5 22:56
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
    public R login(LoginParam loginParam) {
        //参数校验器还没弄，先这样
        BusinessAssert.notEmpty(loginParam.getUsername(), CommonErrorEnums.ILLEGAL_ARGUMENT);
        BusinessAssert.notEmpty(loginParam.getPassword(), CommonErrorEnums.ILLEGAL_ARGUMENT);
        //查询出用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(User.PHONE,loginParam.getUsername());
        queryWrapper.or();
        queryWrapper.eq(User.EMAIL,loginParam.getUsername());
        User user = userService.getOne(queryWrapper);
        log.info("查询出的用户信息:",user);
        BusinessAssert.isTrue(user != null, ErrorEnums.USER_NOT_FOUND);
        //暂时忽略密码摘要
        BusinessAssert.isTrue(user.getPassword().equals(loginParam.getPassword()),ErrorEnums.LOGIN_FAILED);
        //假设登录成功
        return R.success("登录成功");
    }

    /**
     * 注册
     *
     * @param registerParam
     * @return
     */
    @Override
    public R register(RegisterParam registerParam) {
        return null;
    }

    /**
     * 找回密码
     *
     * @param retrieveParam
     * @return
     */
    @Override
    public R retrieve(RetrieveParam retrieveParam) {
        return null;
    }

    /**
     * 修改个人信息
     *
     * @param modifySelfInfoParam
     * @return
     */
    @Override
    public R modifySelfInfo(ModifySelfInfoParam modifySelfInfoParam) {
        return null;
    }
}
