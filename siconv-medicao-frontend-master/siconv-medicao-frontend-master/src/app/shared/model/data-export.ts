export class DataExport {
    columns: string[];
    data: any[];
  
    constructor(columns: string[], data: any[]){
        this.columns = columns;
        this.data = data;
    }
  }
