import { Component, OnInit } from '@angular/core';
import { SharedService } from '../services/shared-service.service';

@Component({
  selector: 'app-not-authorized',
  templateUrl: './not-authorized.component.html',
  styleUrls: ['./not-authorized.component.scss']
})
export class NotAuthorizedComponent implements OnInit {

  constructor(private _sharedService: SharedService) { }

  ngOnInit() {
    // vazio
  }

}
