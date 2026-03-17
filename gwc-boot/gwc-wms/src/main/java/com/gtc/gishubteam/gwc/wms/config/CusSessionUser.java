//package cn.geoair.geoairteam.platform.gtc.config;
//
//import cn.geoair.base.user.GiUserType;
//import cn.geoair.base.user.annotation.GaUser;
//import cn.geoair.base.user.permission.GiPermission;
//import com.gtc.web.permission.GiWebPermissionUser;
//
//@GaUser()
//public class CusSessionUser implements GiWebPermissionUser<Long>{
//
//
//	private static final long serialVersionUID = 1L;
//
//	private String access_token;
//
//
//	public CusSessionUser() {
//	}
//
//	public CusSessionUser(String access_token) {
//		this.access_token=access_token;
//	}
//
//
//	public String getAccess_token() {
//		return access_token;
//	}
//
//	public void setAccess_token(String access_token) {
//		this.access_token = access_token;
//	}
//
//	@Override
//	public boolean isAdmin() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public boolean isTest() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public Long userId() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public GiUserType gtcType() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Class<? extends GiPermission> permissionType() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//
//}
