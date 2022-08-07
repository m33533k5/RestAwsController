import { TestBed } from '@angular/core/testing';

import { AwsIpDataService } from './aws-ip-data.service';

describe('AwsIpDataService', () => {
  let service: AwsIpDataService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AwsIpDataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
