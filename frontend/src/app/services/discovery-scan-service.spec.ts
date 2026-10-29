import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../environments/environment';
import { DiscoveryScanService } from './discovery-scan-service';
import { NetworkContextService } from './network-context-service';

describe('DiscoveryScanService', () => {
  const discoveryScanUrl = `${environment.apiUrl}/networks/3/discovery-scan`;
  let service: DiscoveryScanService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: NetworkContextService, useValue: { getActiveNetworkId: () => 3 } },
      ],
    });
    service = TestBed.inject(DiscoveryScanService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('emits null when the network has not been scanned yet', () => {
    let result: unknown;
    service.getScan().subscribe((scan) => (result = scan));

    httpMock
      .expectOne({ method: 'GET', url: discoveryScanUrl })
      .flush({ detail: 'not found' }, { status: 404, statusText: 'Not Found' });

    expect(result).toBeNull();
  });

  it('rethrows other errors', () => {
    let status: number | undefined;
    service.getScan().subscribe({ error: (err) => (status = err.status) });

    httpMock
      .expectOne({ method: 'GET', url: discoveryScanUrl })
      .flush(null, { status: 500, statusText: 'Server Error' });

    expect(status).toBe(500);
  });

  it('starts a scan with the given range', () => {
    service.startScan({ ipRange: '192.168.0.0/24' }).subscribe();

    const req = httpMock.expectOne({ method: 'POST', url: discoveryScanUrl });
    expect(req.request.body).toEqual({ ipRange: '192.168.0.0/24' });
    req.flush({});
  });
});
