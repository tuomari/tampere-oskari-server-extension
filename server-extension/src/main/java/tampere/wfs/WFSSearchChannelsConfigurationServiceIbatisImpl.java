package tampere.wfs;

import java.io.Reader;
import java.sql.SQLException;
import java.util.List;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import com.ibatis.sqlmap.client.SqlMapSession;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.db.BaseIbatisService;
import tampere.domain.WFSSearchChannelsConfiguration;

public class WFSSearchChannelsConfigurationServiceIbatisImpl extends BaseIbatisService<WFSSearchChannelsConfiguration> implements WFSSearchChannelsConfigurationService{
	private final static Logger log = LogFactory.getLogger(WFSSearchChannelsConfigurationServiceIbatisImpl.class);
	private SqlMapClient client = null;
	
    @Override
    protected String getNameSpace() {
        return "WFSSearchChannelsConfiguration";
    }
    
    public List<WFSSearchChannelsConfiguration> findChannels() {
        List<WFSSearchChannelsConfiguration> channels = queryForList(getNameSpace() + ".findChannels");
        return channels;
    }
    
    
    /*
     * The purpose of this method is to allow many SqlMapConfig.xml files in a
     * single portlet
     */
    protected String getSqlMapLocation() {
        return "META-INF/SqlMapConfig-wfs-search-channels-configuration.xml";
    }
    
    /**
     * Returns SQLmap
     * 
     * @return
     */
    @Override
    protected SqlMapClient getSqlMapClient() {
        if (client != null) {
            return client;
        }

        Reader reader = null;
        try {
            String sqlMapLocation = getSqlMapLocation();
            reader = Resources.getResourceAsReader(sqlMapLocation);
            client = SqlMapClientBuilder.buildSqlMapClient(reader);
            return client;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve SQL client", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    
    public void delete(final int channelId) {
    	long channel_id = Long.valueOf(channelId);
        final SqlMapSession session = openSession();
        try {
            session.startTransaction();
            // remove channel
            session.delete(getNameSpace() + ".delete", channel_id);
            session.commitTransaction();
        } catch (Exception e) {
            new RuntimeException("Error deleting channel with id:" + Long.toString(channel_id), e);
        } finally {
            endSession(session);
        }    	
    };
    
    public synchronized int insert(final WFSSearchChannelsConfiguration channel) {
        SqlMapClient client = null;
        try {
            client = getSqlMapClient();
            client.startTransaction();
            client.insert(getNameSpace() + ".insert", channel);
            Integer id = (Integer) client.queryForObject(getNameSpace() + ".maxId");
            client.commitTransaction();
            return id;
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert", e);
        } finally {
            if (client != null) {
                try {
                    client.endTransaction();
                } catch (SQLException ignored) { }
            }
        }
    }
    
    public void update(final WFSSearchChannelsConfiguration channel) {
    	try {
            getSqlMapClient().update(getNameSpace() + ".update", channel);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update", e);
        }
    };
}
