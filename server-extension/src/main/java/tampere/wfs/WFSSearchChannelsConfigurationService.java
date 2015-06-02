package tampere.wfs;

import java.util.List;
import java.util.Map;

import tampere.domain.WFSSearchChannelsConfiguration;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.BaseService;

public interface WFSSearchChannelsConfigurationService  extends BaseService<WFSSearchChannelsConfiguration>{
    public List<WFSSearchChannelsConfiguration> findChannels();
    public WFSSearchChannelsConfiguration findChannelById(final int channelId);
    public void delete(final int channelId);
    public int insert(final WFSSearchChannelsConfiguration channel);
    public void update(final WFSSearchChannelsConfiguration channel);
}
