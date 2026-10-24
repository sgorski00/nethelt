import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../environments/environment';
import { DeviceService } from './device-service';
import { NetworkContextService } from './network-context-service';

describe('DeviceService', () => {
  const devicesUrl = `${environment.apiUrl}/networks/3/devices`;
  let service: DeviceService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: NetworkContextService, useValue: { getActiveNetworkId: () => 3 } },
      ],
    });
    service = TestBed.inject(DeviceService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('builds url from active network', () => {
    service.deleteDevice(5).subscribe();
    httpMock.expectOne({ method: 'DELETE', url: `${devicesUrl}/5` }).flush(null);
  });

  it('sends only filled filter params', () => {
    service.getDevices({ name: 'router' }).subscribe();

    const req = httpMock.expectOne((r) => r.url === devicesUrl);
    expect(req.request.params.get('name')).toBe('router');
    expect(req.request.params.has('type')).toBe(false);
    expect(req.request.params.has('ip')).toBe(false);
    expect(req.request.params.get('page')).toBe('0');
    req.flush({ content: [] });
  });
});
