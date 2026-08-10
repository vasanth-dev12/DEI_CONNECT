package com.deiconnect.diversity.repository;

import com.deiconnect.diversity.entity.DemographicProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DemographicProfileRepository extends JpaRepository<DemographicProfile, Long> {

    Optional<DemographicProfile> findByEmployee_Id(Long userId);

    boolean existsByEmployee_Id(Long userId);

    @Query("select dp.gender, count(dp) from DemographicProfile dp join dp.employee u "
            + "where dp.consentStatus = com.deiconnect.diversity.enums.ConsentStatus.CONSENTED "
            + "and (:departmentId is null or u.departmentId = :departmentId) "
            + "group by dp.gender")
    List<Object[]> aggregateByGender(@Param("departmentId") Long departmentId);

    @Query("select dp.ethnicity, count(dp) from DemographicProfile dp join dp.employee u "
            + "where dp.consentStatus = com.deiconnect.diversity.enums.ConsentStatus.CONSENTED "
            + "and (:departmentId is null or u.departmentId = :departmentId) "
            + "group by dp.ethnicity")
    List<Object[]> aggregateByEthnicity(@Param("departmentId") Long departmentId);

    @Query("select dp.disability, count(dp) from DemographicProfile dp join dp.employee u "
            + "where dp.consentStatus = com.deiconnect.diversity.enums.ConsentStatus.CONSENTED "
            + "and (:departmentId is null or u.departmentId = :departmentId) "
            + "group by dp.disability")
    List<Object[]> aggregateByDisability(@Param("departmentId") Long departmentId);

    @Query("select dp.veteranStatus, count(dp) from DemographicProfile dp join dp.employee u "
            + "where dp.consentStatus = com.deiconnect.diversity.enums.ConsentStatus.CONSENTED "
            + "and (:departmentId is null or u.departmentId = :departmentId) "
            + "group by dp.veteranStatus")
    List<Object[]> aggregateByVeteran(@Param("departmentId") Long departmentId);

    @Query("select dp.ageGroup, count(dp) from DemographicProfile dp join dp.employee u "
            + "where dp.consentStatus = com.deiconnect.diversity.enums.ConsentStatus.CONSENTED "
            + "and (:departmentId is null or u.departmentId = :departmentId) "
            + "group by dp.ageGroup")
    List<Object[]> aggregateByAgeGroup(@Param("departmentId") Long departmentId);

    @Query("select dp.gender, count(dp) from DemographicProfile dp join dp.employee u "
            + "where dp.consentStatus = com.deiconnect.diversity.enums.ConsentStatus.CONSENTED "
            + "and (:departmentId is null or u.departmentId = :departmentId) "
            + "and u.manager.id = :managerId "
            + "group by dp.gender")
    List<Object[]> aggregateByGenderForManager(@Param("departmentId") Long departmentId,
                                              @Param("managerId") Long managerId);

    @Query("select dp.ethnicity, count(dp) from DemographicProfile dp join dp.employee u "
            + "where dp.consentStatus = com.deiconnect.diversity.enums.ConsentStatus.CONSENTED "
            + "and (:departmentId is null or u.departmentId = :departmentId) "
            + "and u.manager.id = :managerId "
            + "group by dp.ethnicity")
    List<Object[]> aggregateByEthnicityForManager(@Param("departmentId") Long departmentId,
                                                 @Param("managerId") Long managerId);

    @Query("select dp.disability, count(dp) from DemographicProfile dp join dp.employee u "
            + "where dp.consentStatus = com.deiconnect.diversity.enums.ConsentStatus.CONSENTED "
            + "and (:departmentId is null or u.departmentId = :departmentId) "
            + "and u.manager.id = :managerId "
            + "group by dp.disability")
    List<Object[]> aggregateByDisabilityForManager(@Param("departmentId") Long departmentId,
                                                  @Param("managerId") Long managerId);

    @Query("select dp.veteranStatus, count(dp) from DemographicProfile dp join dp.employee u "
            + "where dp.consentStatus = com.deiconnect.diversity.enums.ConsentStatus.CONSENTED "
            + "and (:departmentId is null or u.departmentId = :departmentId) "
            + "and u.manager.id = :managerId "
            + "group by dp.veteranStatus")
    List<Object[]> aggregateByVeteranForManager(@Param("departmentId") Long departmentId,
                                                @Param("managerId") Long managerId);

    @Query("select dp.ageGroup, count(dp) from DemographicProfile dp join dp.employee u "
            + "where dp.consentStatus = com.deiconnect.diversity.enums.ConsentStatus.CONSENTED "
            + "and (:departmentId is null or u.departmentId = :departmentId) "
            + "and u.manager.id = :managerId "
            + "group by dp.ageGroup")
    List<Object[]> aggregateByAgeGroupForManager(@Param("departmentId") Long departmentId,
                                                 @Param("managerId") Long managerId);

    @Query("select dp.gender, count(dp) from DemographicProfile dp join dp.employee u "
            + "where dp.consentStatus = com.deiconnect.diversity.enums.ConsentStatus.CONSENTED "
            + "and (:departmentId is null or u.departmentId = :departmentId) "
            + "and u.hr.id = :hrId "
            + "group by dp.gender")
    List<Object[]> aggregateByGenderForHr(@Param("departmentId") Long departmentId,
                                          @Param("hrId") Long hrId);

    @Query("select dp.ethnicity, count(dp) from DemographicProfile dp join dp.employee u "
            + "where dp.consentStatus = com.deiconnect.diversity.enums.ConsentStatus.CONSENTED "
            + "and (:departmentId is null or u.departmentId = :departmentId) "
            + "and u.hr.id = :hrId "
            + "group by dp.ethnicity")
    List<Object[]> aggregateByEthnicityForHr(@Param("departmentId") Long departmentId,
                                             @Param("hrId") Long hrId);

    @Query("select dp.disability, count(dp) from DemographicProfile dp join dp.employee u "
            + "where dp.consentStatus = com.deiconnect.diversity.enums.ConsentStatus.CONSENTED "
            + "and (:departmentId is null or u.departmentId = :departmentId) "
            + "and u.hr.id = :hrId "
            + "group by dp.disability")
    List<Object[]> aggregateByDisabilityForHr(@Param("departmentId") Long departmentId,
                                              @Param("hrId") Long hrId);

    @Query("select dp.veteranStatus, count(dp) from DemographicProfile dp join dp.employee u "
            + "where dp.consentStatus = com.deiconnect.diversity.enums.ConsentStatus.CONSENTED "
            + "and (:departmentId is null or u.departmentId = :departmentId) "
            + "and u.hr.id = :hrId "
            + "group by dp.veteranStatus")
    List<Object[]> aggregateByVeteranForHr(@Param("departmentId") Long departmentId,
                                           @Param("hrId") Long hrId);

    @Query("select dp.ageGroup, count(dp) from DemographicProfile dp join dp.employee u "
            + "where dp.consentStatus = com.deiconnect.diversity.enums.ConsentStatus.CONSENTED "
            + "and (:departmentId is null or u.departmentId = :departmentId) "
            + "and u.hr.id = :hrId "
            + "group by dp.ageGroup")
    List<Object[]> aggregateByAgeGroupForHr(@Param("departmentId") Long departmentId,
                                            @Param("hrId") Long hrId);

    @Query("select dp.gender, u.salary, u.yearsOfExperience from DemographicProfile dp join dp.employee u "
            + "where dp.consentStatus = com.deiconnect.diversity.enums.ConsentStatus.CONSENTED "
            + "and u.salary is not null and dp.gender is not null")
    List<Object[]> genderSalaryRows();

    @Query("select dp.gender, u.salary, u.yearsOfExperience from DemographicProfile dp join dp.employee u "
            + "where dp.consentStatus = com.deiconnect.diversity.enums.ConsentStatus.CONSENTED "
            + "and u.salary is not null and dp.gender is not null and u.hr.id = :hrId")
    List<Object[]> genderSalaryRowsForHr(@Param("hrId") Long hrId);

    @Query("select dp.ethnicity, u.salary, u.yearsOfExperience from DemographicProfile dp join dp.employee u "
            + "where dp.consentStatus = com.deiconnect.diversity.enums.ConsentStatus.CONSENTED "
            + "and u.salary is not null and dp.ethnicity is not null")
    List<Object[]> ethnicitySalaryRows();

    @Query("select dp.ethnicity, u.salary, u.yearsOfExperience from DemographicProfile dp join dp.employee u "
            + "where dp.consentStatus = com.deiconnect.diversity.enums.ConsentStatus.CONSENTED "
            + "and u.salary is not null and dp.ethnicity is not null and u.hr.id = :hrId")
    List<Object[]> ethnicitySalaryRowsForHr(@Param("hrId") Long hrId);
}
