package com.dotcms.sass;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;

import com.dotmarketing.beans.Host;
import com.dotmarketing.beans.Identifier;
import com.dotmarketing.beans.VersionInfo;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.IdentifierAPI;
import com.dotmarketing.business.web.HostWebAPI;
import com.dotmarketing.business.web.UserWebAPI;
import com.dotmarketing.business.web.WebAPILocator;
import com.dotmarketing.portlets.contentlet.business.ContentletAPI;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.fileassets.business.FileAsset;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.InodeUtils;
import com.dotmarketing.util.Logger;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.liferay.portal.model.User;


public class SassServlet extends HttpServlet {
    private static final IdentifierAPI identAPI = APILocator.getIdentifierAPI();
    private static final ContentletAPI contAPI = APILocator.getContentletAPI();
    private static final long defLangId = APILocator.getLanguageAPI().getDefaultLanguage().getId();
    private static final UserWebAPI userWebAPI = WebAPILocator.getUserWebAPI();
    private static final long serialVersionUID = 4071173631204980076L;
    private static HostWebAPI hostWebAPI = WebAPILocator.getHostWebAPI();
    
    
    private static Cache<String, HashMap<String, Object>> lastModifiedMap = null;
    
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String css=null;
        try {
            Host host = hostWebAPI.getCurrentHost(req);
            User user = userWebAPI.getLoggedInUser(req);
            String uri=req.getRequestURI();
            
            Identifier ident=identAPI.find(host, uri);
            if(ident!=null && InodeUtils.isSet(ident.getInode())) {
            	
            	
            	VersionInfo vi = APILocator.getVersionableAPI().getVersionInfo(ident.getInode());
            	HashMap<String, Object> lastMod = lastModifiedMap.get(ident.getInode());
            	
            	// if we have a file that has been modified
            	if(lastMod ==null || ((Date) lastMod.get("DATE")).before(vi.getVersionTs())){
	                Contentlet cont = contAPI.findContentletByIdentifier(
	                        ident.getId(), true, defLangId, user, true);
	                if(cont!=null && InodeUtils.isSet(cont.getInode())) {
	                    FileAsset asset=APILocator.getFileAssetAPI().fromContentlet(cont);
	                    String code=FileUtils.readFileToString(asset.getFileAsset());
	                    SassEngine.Syntax syntax=uri.endsWith(".sass") ?
	                            SassEngine.Syntax.sass : SassEngine.Syntax.scss;
	                    css=SassEngine.evalSass(code, syntax, 
	                            this.getServletContext().getRealPath("WEB-INF/jruby-libs"), 
	                            this.getServletContext().getRealPath("WEB-INF/sass-gem/lib"));
	                    lastMod = new HashMap<String, Object>();
	                    lastMod.put("DATE", vi.getVersionTs());
	                    lastMod.put("CSS", css);
	                    lastModifiedMap.put(ident.getInode(), lastMod);
	                }
            	}
            	else if(lastMod !=null && ((Date) lastMod.get("DATE")).after(vi.getVersionTs())){
            		css = (String) lastMod.get("CSS");
            	}
            }
        }
        catch(Exception ex) {
            Logger.warn(this, ex.getMessage(), ex);
            throw new ServletException(ex);
        }
        if(css!=null) {
            resp.getWriter().print(css);
        }
        else {
            resp.sendError(404);
        }
    }

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		// 10 css files by default
		CacheBuilder<Object, Object> cb  = CacheBuilder
				.newBuilder()
				.maximumSize(Config.getIntProperty("cache.sass.size", 10))
				.concurrencyLevel(Config.getIntProperty("cache.concurrencylevel", 32));
				

		
		lastModifiedMap = cb.build();
		
		
		
	}
    
    
    
}
