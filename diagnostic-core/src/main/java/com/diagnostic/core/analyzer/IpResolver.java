package com.diagnostic.core.analyzer;

import java.util.Map;

public interface IpResolver {
    
    IpLocation resolve(String ip);
    
    class IpLocation {
        private String country;
        private String region;
        private String province;
        private String city;
        private String isp;
        
        public IpLocation() {
        }
        
        public IpLocation(String country, String region, String province, String city, String isp) {
            this.country = country;
            this.region = region;
            this.province = province;
            this.city = city;
            this.isp = isp;
        }
        
        public String getCountry() {
            return country;
        }
        
        public void setCountry(String country) {
            this.country = country;
        }
        
        public String getRegion() {
            return region;
        }
        
        public void setRegion(String region) {
            this.region = region;
        }
        
        public String getProvince() {
            return province;
        }
        
        public void setProvince(String province) {
            this.province = province;
        }
        
        public String getCity() {
            return city;
        }
        
        public void setCity(String city) {
            this.city = city;
        }
        
        public String getIsp() {
            return isp;
        }
        
        public void setIsp(String isp) {
            this.isp = isp;
        }
    }
}
