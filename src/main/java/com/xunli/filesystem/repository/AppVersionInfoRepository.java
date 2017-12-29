package com.xunli.filesystem.repository;

import com.xunli.filesystem.model.AppVersionInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Created by shihj on 2017/12/25.
 */
@Repository
public interface AppVersionInfoRepository extends JpaRepository<AppVersionInfo,Long>,JpaSpecificationExecutor<Long>{
    AppVersionInfo findTopByIfUseOrderByCurrentVersionDesc(String ifUse);
}
