import { Component, OnInit } from '@angular/core';
import {AwsIpDataService} from "../../service/aws-ip-data.service";
import {AwsIpData} from "../../awsIpData";

@Component({
  selector: 'app-data',
  templateUrl: './data.component.html',
  styleUrls: ['./data.component.scss']
})
export class DataComponent implements OnInit {

  awsIpData: AwsIpData[] = [];

  constructor(
    private awsIpDataService: AwsIpDataService,
  ) { }

  ngOnInit(): void {
  }

  getAwsIpData(): void {
    this.awsIpDataService.findAll().subscribe(data => this.awsIpData = data)
  }

}
