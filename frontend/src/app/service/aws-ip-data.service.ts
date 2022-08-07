import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {AwsIpData} from "../awsIpData";

@Injectable({
  providedIn: 'root'
})
export class AwsIpDataService {

  private dataUrl: string = "http://localhost:8081/ip/?region=ALL"

  constructor(
    private httpClient: HttpClient
  ) { }

  findAll(): Observable<AwsIpData[]>{
    return this.httpClient.get<AwsIpData[]>(this.dataUrl)
  }
}
