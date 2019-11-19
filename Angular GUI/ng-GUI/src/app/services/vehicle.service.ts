import { Injectable } from '@angular/core';
import {HttpClient, HttpEvent, HttpRequest} from '@angular/common/http';
import {Observable} from 'rxjs';


@Injectable({
  providedIn: 'root'
})

export class VehicleService {


  // get endpoint
  endpointURL: any =  "http://localhost:4567/hello";      // back-end URL
  // service makes an HTTP call to the backend


  constructor(private http: HttpClient) { }

  getData(): Observable<any> {
    return this.http.get(this.endpointURL);     // getting JSON from the backend
  }


}