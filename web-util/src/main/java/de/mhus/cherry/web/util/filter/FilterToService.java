package de.mhus.cherry.web.util.filter;

import de.mhus.cherry.web.api.InternalCallContext;
import de.mhus.cherry.web.api.VirtualHost;
import de.mhus.cherry.web.api.WebFilter;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.logging.MLogUtil;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.services.MOsgi;

public class FilterToService implements WebFilter {

	private IConfig config;
	private String serviceName;
	private WebFilter webFilter;
	private VirtualHost vHost;

	@Override
	public void doInitialize(VirtualHost vHost, IConfig config) throws MException {
		this.config = config.getNode("config");
		serviceName = config.getString("service");
		this.vHost = vHost;
	}

	@Override
	public boolean doFilterBegin(InternalCallContext call) throws MException {
		check();
		if (webFilter == null) throw new NotFoundException("service not found",serviceName);
		return webFilter.doFilterBegin(call);
	}

	@Override
	public void doFilterEnd(InternalCallContext call) throws MException {
		check();
		if (webFilter == null) throw new NotFoundException("service not found",serviceName);
		webFilter.doFilterEnd(call);
	}

	private synchronized void check() {
		if (webFilter == null) {
			try {
				webFilter = MOsgi.getService(WebFilter.class,"(name=" + serviceName + ")");
				webFilter.doInitialize(vHost, config);
			} catch (Throwable e) {
				MLogUtil.log().e(serviceName,e);
			}
		}
	}

}
