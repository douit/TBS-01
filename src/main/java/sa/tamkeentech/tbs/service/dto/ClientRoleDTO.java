package sa.tamkeentech.tbs.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientRoleDTO implements Serializable{

    private RoleDTO role;

    private ClientDTO client;

}
