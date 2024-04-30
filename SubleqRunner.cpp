#include <iostream>
#include <fstream>
#include <vector>
#include <unordered_map>
#include <string>
#include <sstream>
class SubleqRunner{
    int halt = -1;
    std::vector<int> memory;
    std::unordered_map<std::string,int> vars;
    int startPoint;
    public:
    
    bool loadAndRun(std::string fileName){
        std::fstream file;
        file.open(fileName.c_str(),std::ios::in);
        if(file.is_open()){
            std::string tmp;
        
            int i = 0;
         
            startPoint = i;
          
            while(getline(file,tmp)){
                if (tmp == "}"){
                    break;
                }
                if(tmp[0]== ';'){
                   
                    auto space = tmp.find(" ");
                    vars[tmp.substr(1,space-1)] = i;
                    int val = std::stoi(tmp.substr(space));
                    memory.push_back(val);
                    i+=1;
                    startPoint = i;
                }
                else if (tmp != "{"){
                    i = splitString(tmp,i);
                }
             
            }
        }
        else{
            std::cout<<"OOO";
        }
        
        runner(startPoint);
        return true;
    }

    private:
        bool oob(int pc){
            if(pc > memory.size()){
                return true;
            }return false;
        }
        int splitString(std::string s,int &pc){
           
            int count =0;
            std::string curr ;
            std::stringstream st(s);
            while( st >> curr){
              
               
                    if (vars.contains(curr)){
                        memory.push_back(vars.at(curr));
                    }
                    else{
                        memory.push_back(std::stoi(curr));
                    }
                pc +=1;
                count +=1;
            }
            if(count < 3){
                pc+=1;
                memory.push_back(pc);
                
            }
            return pc;
        }
    
    void runner(int start){
        int counter,lop,rop,third,after;
        char c;
        counter = start;
        while(counter >=0){
            after = counter +3;
            if(oob(after)){
                break;
            }
            lop = memory[counter];
            rop = memory[counter+1];
            third = memory[counter+2];
            
            if(lop == -1){
               
                std::cin >> c;
                memory[rop] = (int)c;

            }
            else if(rop == -1){
                char c = (char) memory.at(lop);
                std::cout<<c;
            }
            else{
                memory[rop] -= memory[lop];
                if(memory[rop] <= 0){
                    after = third;
                }
            }
            
          
            counter = after;
            
            
            
        }
        
    }
};
int main(int argc, char* argv[]){
        SubleqRunner s;
        std::string toRun;
        if (argc > 1 ){
            toRun =argv[1];
        }
        
        
        s.loadAndRun("/Users/quinnrafferty_1_2/Development/Programming_Languages/TermProject/hello.asq");
         
        
        return 0;
    }
