package com.dotcms.sass;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;

import com.dotmarketing.beans.Host;
import com.dotmarketing.beans.Identifier;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.IdentifierAPI;
import com.dotmarketing.business.web.HostWebAPI;
import com.dotmarketing.business.web.UserWebAPI;
import com.dotmarketing.business.web.WebAPILocator;
import com.dotmarketing.portlets.contentlet.business.ContentletAPI;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.fileassets.business.FileAsset;
import com.dotmarketing.util.InodeUtils;
import com.dotmarketing.util.Logger;
import com.liferay.portal.model.User;


public class SassServlet extends HttpServlet {
    private static final IdentifierAPI identAPI = APILocator.getIdentifierAPI();
    private static final ContentletAPI contAPI = APILocator.getContentletAPI();
    private static final long defLangId = APILocator.getLanguageAPI().getDefaultLanguage().getId();
    private static final UserWebAPI userWebAPI = WebAPILocator.getUserWebAPI();
    private static final long serialVersionUID = 4071173631204980076L;
    private static HostWebAPI hostWebAPI = WebAPILocator.getHostWebAPI();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String css=null;
        try {
            Host host = hostWebAPI.getCurrentHost(req);
            User user = userWebAPI.getLoggedInUser(req);
            String uri=req.getRequestURI();
            
            Identifier ident=identAPI.find(host, uri);
            if(ident!=null && InodeUtils.isSet(ident.getInode())) {
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
}
