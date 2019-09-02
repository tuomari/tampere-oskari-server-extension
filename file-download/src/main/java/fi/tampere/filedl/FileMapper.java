package fi.tampere.filedl;

import org.apache.ibatis.annotations.*;

import java.util.List;

public interface FileMapper {

        @Select("SELECT id,"
                + "feature_id,"
                + "locale,"
                + "file_extension "
                + "FROM tampere_layer_attachment "
                + "WHERE layer_id = #{id}")
        List<WFSAttachmentFile> findByLayer(@Param("id") int id);
}
