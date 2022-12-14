package com.cardealer.cars.web.controller;

import com.cardealer.cars.common.OutputJson;
import com.cardealer.cars.model.binding.UserLoginBindingModel;
import com.cardealer.cars.model.binding.UserRegisterBindingModel;
import com.cardealer.cars.model.service.UserLoginServiceModel;
import com.cardealer.cars.model.service.UserRegisterServiceModel;
import com.cardealer.cars.model.view.profile.MyProfileView;
import com.cardealer.cars.service.UserDetailsService;
import com.cardealer.cars.service.UserService;
import com.cardealer.cars.util.ValidationUtil;
import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

@RestController
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper;
    private final Gson gson;
    private final OutputJson outputJson;
    private final UserDetailsService userDetailsService;
    private final ValidationUtil validationUtil;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, ModelMapper modelMapper, Gson gson, OutputJson outputJson, UserDetailsService userDetailsService,
                          ValidationUtil validationUtil, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.gson = gson;
        this.outputJson = outputJson;
        this.userDetailsService = userDetailsService;
        this.validationUtil = validationUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @CrossOrigin
    @PostMapping("/register")
    public OutputJson register(
        @RequestBody
            String registerForm) {
        UserRegisterBindingModel userRegisterBindingModel = gson.fromJson(registerForm, UserRegisterBindingModel.class);
        if (validationUtil.isValid(userRegisterBindingModel)) {
            if (!userRegisterBindingModel
                .getPassword()
                .equals(userRegisterBindingModel.getConfirmPassword())) {
                outputJson.setSuccess(false);
                outputJson.setMessage("???????????????? ???? ????????????????");
                return outputJson;
            }
            //            else if (Period.between(userRegisterBindingModel.getBirthdate(), LocalDate.now()).getYears() < 18) {
            //                outputJson.setSuccess(false);
            //                outputJson.setMessage("?????????????????????? ?????????????? ???? ?????????????????????? ?? 18 ????????????");
            //                return outputJson;
            //            }
            else if (userDetailsService.findPlayerByEmail(userRegisterBindingModel.getEmail()) != null) {
                outputJson.setSuccess(false);
                outputJson.setMessage("???????? ?? ?????????????????????? ?????????? ?? ???????? ?????????? ??????????");
                return outputJson;
            }
            userRegisterBindingModel.setEmail(userRegisterBindingModel.getEmail());
            userRegisterBindingModel.setPassword(passwordEncoder.encode(userRegisterBindingModel.getPassword()));
            userService.register(modelMapper.map(userRegisterBindingModel, UserRegisterServiceModel.class));
            outputJson.setSuccess(true);
            outputJson.setMessage("?????????????? ???? ??????????????????????????!");
            return outputJson;
        }
        StringBuilder sb = new StringBuilder();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<UserRegisterBindingModel>> violations = validator.validate(userRegisterBindingModel);
        for (ConstraintViolation<UserRegisterBindingModel> violation : violations) {
            sb
                .append(violation.getMessage())
                .append(System.lineSeparator());
        }
        outputJson.setSuccess(false);
        outputJson.setMessage(sb.toString());
        return outputJson;
    }

    @CrossOrigin
    @PostMapping("/signin")
    public OutputJson loginConfirm(
        @RequestBody
            String loginForm) {
        UserLoginBindingModel userLoginBindingModel = gson.fromJson(loginForm, UserLoginBindingModel.class);
        if (validationUtil.isValid(userLoginBindingModel)) {
            UserLoginServiceModel userLoginServiceModel = userService.findByEmailAndPassword(userLoginBindingModel.getEmail(),
                                                                                             userLoginBindingModel.getPassword());
            if (userLoginServiceModel == null) {
                outputJson.setSuccess(false);
                outputJson.setMessage("?????????????????? ?????????? ?????????? ?????? ????????????");
                return outputJson;
            }
            //        if (!userDetailsService.checkEmailConfirmed(userLoginBindingModel.getEmail())) {
            //            outputJson.setSuccess(false);
            //            outputJson.setMessage("???????????? ???? ???????????????????? ???????????? ????");
            //            return outputJson;
            //        }
            outputJson.setSuccess(true);
            outputJson.setMessage("?????????????? ???? ????????????????");
            outputJson.setUserId(userLoginServiceModel.getId());
            outputJson.setName(userLoginServiceModel.getName());
            return outputJson;
        }
        StringBuilder sb = new StringBuilder();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<UserLoginBindingModel>> violations = validator.validate(userLoginBindingModel);
        for (ConstraintViolation<UserLoginBindingModel> violation : violations) {
            sb
                .append(violation.getMessage())
                .append(System.lineSeparator());
        }
        outputJson.setSuccess(false);
        outputJson.setMessage(sb.toString());
        return outputJson;
    }

    @CrossOrigin
    @GetMapping("/myprofile/{id}")
    public OutputJson myProfile(
        @PathVariable
            Long id) {
        OutputJson outputJson = new OutputJson();
        MyProfileView myProfileByPlayerDetailsId = userService.getMyProfile(id);
        if (myProfileByPlayerDetailsId == null) {
            outputJson.setSuccess(false);
            outputJson.setMessage("???????? ??????????");
            return outputJson;
        }
        outputJson.setSuccess(true);
        outputJson.setMyProfileView(myProfileByPlayerDetailsId);
        return outputJson;
    }

    @CrossOrigin
    @PostMapping("/login")
    public void tokenLogin() {
    }

    @CrossOrigin
    @GetMapping("/confirm")
    public OutputJson confirm(
        @RequestParam("token")
            String token) {
        return userService.confirmToken(token);
    }

    @CrossOrigin
    @PostMapping("/updatePictureUrl/{id}")
    public boolean confirm(
        @PathVariable
            Long id,
        @RequestBody
            String pictureUrl) {
        String pictureUrlString = gson.fromJson(pictureUrl, String.class);
        return userService.updatePictureUrl(id, pictureUrlString);
    }

}
