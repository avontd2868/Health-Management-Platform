/**
 * Singleton for tracking the current user.
 */
Ext.define('org.osehra.hmp.UserContext', {
    uses: [
        'org.osehra.hmp.appbar.ErrorManager'
    ],
    singleton:true,
    mixins:{
        observable:'Ext.util.Observable'
    },
    config:{
        /**
         * @cfg {Object} userInfo
         * @property {String} userInfo.displayName
         * @property {String} userInfo.duz
         * @property {String} userInfo.division
         * @property {String} userInfo.divisionName
         * @property {String} userInfo.serviceSection
         * @property {Number} userInfo.timeoutSeconds
         * @property {Number} userInfo.timeoutCountdownSeconds
         * @property {String} userInfo.title
         * @property {String} userInfo.uid
         * @property {Array} userInfo.vistaSecurityKeys
         */
        userInfo:{
            displayName:'Guest'
        },
        /**
         * @cfg {Object} userPrefs
         */
        userPrefs: {
            'cpe.patientpicker.loc': 'north',
            'cpe.patientpicker.pinned': false,
            'cpe.patientpicker.animateOption': 'delay',
            'cpe.patientpicker.animateDelaySeconds': '1000',
            'cpe.patientpicker.defaultRosterID': null,
            'cpe.patientpicker.rememberlast': true,
            'cpe.patientpicker.mask': false,
            'cpe.patientpicker.hash': false,
            'cpe.editmode': true
        }
    },
    lastActivity:new Date().getTime(),
    constructor:function (cfg) {
        var me = this;

        me.mixins.observable.constructor.call(this);
        me.initConfig(cfg);

        me.addEvents(
            /**
             * @event userchange
             * Fires when the current user changes
             * @param {Object} userInfo
             */
            'userchange'
        );

        // task for warning if the session is about to expire
        me.timeoutWarnTask = {
            interval:1000,
            run:me.checkForInactivity,
            scope: me
        }
    },
    /**
     *
     * @return {Boolean}
     */
    isAuthenticated:function () {
        var displayName = this.getUserInfo().displayName;
        return displayName != 'Guest';
    },
    /**
     *
     * @return {Number}
     */
    getTimeoutSeconds:function () {
        var seconds = this.getUserInfo().timeoutSeconds;
        if (!Ext.isDefined(seconds)) return 0;
        return seconds;
    },
    /**
     *
     * @return {Number}
     */
    getTimeoutCountdownSeconds:function () {
        var seconds = this.getUserInfo().timeoutCountdownSeconds;
        if (!Ext.isDefined(seconds)) return 0;
        return seconds;
    },
    /**
     *
     * @param {String} key
     * @return {Boolean}
     */
    currentUserHasVistaKey: function(key) {
        var authority = "VISTA_KEY_" + key.split(" ").join("_");
        return this.currentUserHasAuthority(authority);
    },
    /**
     *
     * @param {String} authority
     * @return {Boolean}
     */
    currentUserHasAuthority: function(authority) {
        return Ext.Array.contains(this.getUserInfo().authorities || [], authority);
    },
    /**
     * @private
     */
    applyUserInfo:function (userInfo) {
        var me = this;

        me.userInfo = userInfo;
        me.fireEvent('userchange', this.userInfo);

        // if there is a timeout warning/setting then start the necessary tasks
        if (me.getTimeoutCountdownSeconds() > 0 && me.getTimeoutSeconds() > 0) {
            me.lastActivity = new Date().getTime();

//            Ext.log("starting user inactivity monitor for " + me.getTimeoutSeconds() + " seconds");
            Ext.TaskManager.start(me.timeoutWarnTask);

            // use global click/key handlers to reset the inactivity timeout
            Ext.getBody().on('click', me.userActivity, me);
            Ext.getBody().on('keydown', me.userActivity, me);
            Ext.getBody().on('mousemove', me.userActivity, me);
        }
    },
    
    /**
     * protect from setting prefs to null
     */
    applyUserPrefs:function(userPrefs){
    	return (!userPrefs)?this.userPrefs:userPrefs;
    },
     
    /**
     * @private
     */
    checkForInactivity:function() {
        var me = this;
        // the cookie that will keep the session alive (developer mode only)
        //Ext.util.Cookies.set('KEEPALIVE', '1', new Date(2020,1,1));
        var keepalive = Ext.util.Cookies.get('KEEPALIVE');
        var elapsed = (new Date().getTime() - this.lastActivity) / 1000;
//        Ext.log("elapsed: " + elapsed);
        var remaining = Ext.util.Format.round(me.getTimeoutCountdownSeconds() + me.getTimeoutSeconds() - elapsed, 0);

        if (remaining <= 0) {
            window.location = '/auth/logout'
        } else if (remaining <= me.getTimeoutCountdownSeconds() && keepalive) {
            me.userActivity();
            org.osehra.hmp.appbar.ErrorManager.warn('Keepalive', 5000);
            Ext.Ajax.request({
                url:'/auth/keepalive',
                params:{
                    id:Ext.Date.now()
                },
                success:function (response) {
                    // TODO: Don't ignore?
                }
            });
        } else if (remaining <= me.getTimeoutCountdownSeconds()) {
            org.osehra.hmp.appbar.ErrorManager.warn('Your session will expire in ' + remaining + ' seconds.', 1500);
        }
    },
    /**
     * @private
     */
    userActivity:function() {
        this.lastActivity = new Date().getTime();
//        Ext.log('not idle.');
    }     
});
