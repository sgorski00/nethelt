import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { NetworkResponse } from '../models/network/network-response';
import { NetworkContextService } from './network-context-service';
import { NetworkService } from './network-service';

describe('NetworkContextService', () => {
  const network: NetworkResponse = { id: 7, name: 'Home', description: null, createdAt: '' };
  const getNetwork = vi.fn();
  let service: NetworkContextService;

  beforeEach(() => {
    localStorage.clear();
    getNetwork.mockReset();
    TestBed.configureTestingModule({
      providers: [{ provide: NetworkService, useValue: { getNetwork } }],
    });
    service = TestBed.inject(NetworkContextService);
  });

  it('stores active network', () => {
    service.setActiveNetwork(network);

    expect(service.activeNetwork()).toEqual(network);
    expect(service.getActiveNetworkId()).toBe(7);
  });

  it('clears active network', () => {
    service.setActiveNetwork(network);
    service.clear();

    expect(service.activeNetwork()).toBeNull();
    expect(service.getActiveNetworkId()).toBeNull();
  });

  it('loads stored network only once', () => {
    localStorage.setItem('activeNetworkId', '7');
    getNetwork.mockReturnValue(of(network));

    service.load();
    service.load();

    expect(getNetwork).toHaveBeenCalledTimes(1);
    expect(getNetwork).toHaveBeenCalledWith(7);
    expect(service.activeNetwork()).toEqual(network);
  });

  it('clears stored network when it cannot be loaded', () => {
    localStorage.setItem('activeNetworkId', '7');
    getNetwork.mockReturnValue(throwError(() => new Error('not found')));

    service.load();

    expect(service.getActiveNetworkId()).toBeNull();
  });

  it('does not load anything without stored network', () => {
    service.load();
    expect(getNetwork).not.toHaveBeenCalled();
  });
});
