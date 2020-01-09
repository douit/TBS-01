package sa.tamkeentech.tbs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sa.tamkeentech.tbs.domain.Client;
import sa.tamkeentech.tbs.repository.ClientRepository;
import sa.tamkeentech.tbs.service.dto.ClientDTO;
import sa.tamkeentech.tbs.service.mapper.ClientMapper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing {@link Client}.
 */
@Service
@Transactional
public class ClientService {

    private final Logger log = LoggerFactory.getLogger(ClientService.class);

    private final ClientRepository clientRepository;

    private final ClientMapper clientMapper;
    private final UserService userService;

    public ClientService(ClientRepository clientRepository, ClientMapper clientMapper, UserService userService) {
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
        this.userService = userService;
    }

    /**
     * Save a client.
     *
     * @param clientDTO the entity to save.
     * @return the persisted entity.
     */
    public ClientDTO save(ClientDTO clientDTO) {
        log.debug("Request to save Client : {}", clientDTO);
        Client client = clientMapper.toEntity(clientDTO);
        client = clientRepository.save(client);
        return clientMapper.toDto(client);
    }

    /**
     * Get all the clients.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<ClientDTO> findAll() {
        log.debug("Request to get all Clients");
        return clientRepository.findAll().stream()
            .map(clientMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }
    /**
     * Get client based on role.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<ClientDTO> findByRole() {
        log.debug("Request to get  Clients based on role");
        List<Long> clientIds = userService.listClientIds(0);
        List<ClientDTO> clients = new ArrayList();

        for(Long clientId : clientIds){
            clients.add(findOne(clientId).get());
        }
        return clients;
    }


    /**
     * Get one client by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ClientDTO> findOne(Long id) {
        log.debug("Request to get Client : {}", id);
        return clientRepository.findById(id)
            .map(clientMapper::toDto);
    }

    /**
     * Delete the client by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Client : {}", id);
        clientRepository.deleteById(id);
    }

    public Optional<Client> getClientBySecretKey(String secret) {
        return clientRepository.findByClientSecret(secret);
    }

    public Optional<Client> getClientById(Long id) {
        return clientRepository.findById(id);
    }

    public Optional<Client> getClientByClientId(String appName) {
        return clientRepository.findByClientId(appName);
    }
}
