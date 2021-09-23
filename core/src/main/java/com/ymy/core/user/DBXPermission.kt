package com.ymy.core.user


/**
 * Created on 2020/10/19 13:37.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object DBXPermission {

    fun checkHasDBXPermission(checkPermission: Long) =
        YmyUserManager.checkPermission(checkPermission)

    /**
     *全部权限
     */
    const val app_all: Long = 50001

    /**
     *报警
     */
    const val app_alarm: Long = 50002

    /**
     *预案演练
     */
    const val app_emergency: Long = 50003

    /**
     *维护保养
     */
    const val app_maintain: Long = 50004

    /**
     *隐患排查
     */
    const val app_troubleshoot: Long = 50005

    /**
     *中控交接
     */
    const val app_handover: Long = 50006

    /**
     *消防主机记录
     */
    const val app_host_log: Long = 50007

    /**
     *消防记录
     */
    const val app_fire_rec: Long = 50008

    /**
     *通知通告
     */
    const val app_notice: Long = 50009

    /**
     *网关状态
     */
    const val app_gateway: Long = 50010

    /**
     *企业管理
     */
    const val app_org_cfg: Long = 50011

    /**
     *一周小结
     */
    const val app_weekly_summary: Long = 50012

    /**
     *数据分析
     */
    const val app_data_analysis: Long = 50013

    /**
     *上门服务
     */
    const val app_on_site_service: Long = 50014

    /**
     *重点部位巡查
     */
    const val app_patrol: Long = 50015

    /**
     *企业信息
     */
    const val app_org_cfg_org_info: Long = 50016

    /**
     *部门与成员管理
     */
    const val app_org_cfg_dpt_users: Long = 50017

    /**
     *角色管理
     */
    const val app_org_cfg_roles_cfg: Long = 50018

    /**
     *新成员加入
     */
    const val app_org_cfg_invite: Long = 50019

    /**
     *报警设置
     */
    const val app_org_cfg_alarm_cfg: Long = 50020

    /**
     *设备管理
     */
    const val app_org_cfg_device_cfg: Long = 50021

    /**
     *预案演练设置
     */
    const val app_org_cfg_emergency_cfg: Long = 50022

    /**
     *消防培训管理
     */
    const val app_org_cfg_edu_cfg: Long = 50023

    /**
     *隐患排查设置
     */
    const val app_org_cfg_troubleshoot_cfg: Long = 50024

    /**
     *重点部位巡查设置
     */
    const val app_org_cfg_patrol_cfg: Long = 50025

    /**
     *维护保养设置
     */
    const val app_org_cfg_maintain_cfg: Long = 50026

    /**
     *工单设置
     */
    const val app_org_cfg_work_order_cfg: Long = 50027

    /**
     *消防主机信号设置
     */
    const val app_org_cfg_host_sig_cfg: Long = 50028

    /**
     *通知通告设置
     */
    const val app_org_cfg_notice_cfg: Long = 50029

    /**
     *消防记录设置
     */
    const val app_org_cfg_fire_rec_cfg: Long = 50030

    /**
     *中控交接设置
     */
    const val app_org_cfg_handover_cfg: Long = 50031

    /**
     *消防档案管理
     */
    const val app_org_cfg_fire_doc_cfg: Long = 50032
}

