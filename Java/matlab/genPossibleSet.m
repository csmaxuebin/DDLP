function [DeltaX, state_no_vec,size_deltax] = genPossibleSet(T, p_prior, true_loc, state_no, delta, setting)
%if setting==1, gen possible set by pr>delta;
%if setting==2, gen possible set by dist<delta;
%if setting==3, gen with graph k-neighbors;
%if setting==4, gen with graph category
%if setting==5, gen with custome graph of graph_name;
%generate the possible set;
%return a m x N matrix where N is state- dimension , m is how many possible;
%For example, [1 0 0 ; 0 0 1] means #1 and #3 are possible in 3-state Markov;

% call order: genPossibleSet -> IM_Release -> IM_inference



N=size(p_prior,2);% state size;
bool_p=zeros(1,N);


map_loc=T*true_loc';%the map coordinate of true location;


if setting==1
%     if(p_prior(state_no)==0)%add true location;
%         p_prior(state_no)=0.001;
%     end
%    bool_p=p_prior>0;
    
%     C0=zeros(sum(bool_p),N);
%     temp=[];
%     for i=1:N
%         temp=[temp,bool_p(1,i)];
%         row=sum(temp);
%         if bool_p(1,i)==1
%             C0(row,i)=1;
%         end
%     end
%     
%     %test
% %     if(size(C0,1)==0)
% %         fprintf('\n C0 is null ! \n');
% %     end
%     
%     d_max=0;%maximum distance from true location;
%     for j=1:size(C0,1)
%         if(dist(map_loc,T*C0(j,:)')>d_max)
%             d_max=dist(map_loc,T*C0(j,:)');
%         end
%     end
%     
%     %fitness function;
%     fitness=zeros(size(C0,1),1);
%     for i=1:size(C0,1)
%         fitness(i)=sum(p_prior.*C0(i,:))+delta*( d_max-dist(map_loc,T*C0(i,:)') )/d_max;
%     end
%     
%     
%     %genetic selection;
%     rest_candidate=sum(C0);
%     xi0=xi;
%     if(sum(rest_candidate)<xi)
%         xi0=sum(rest_candidate);
%     end
%     DeltaX=[];
%     
%     %test
% %     if(size(rest_candidate,1)==0)
% %         fprintf('\n C0 is null ! \n');
% %     end
%     
%     while(rest_candidate(state_no)==1 || size(DeltaX,1)<xi0)
%         
%         fitness0=fitness./sum(fitness);%normalize;
%         for i=1:size(fitness0,1)
%             fitness(i)=sum(fitness0(1:i));
%         end
%         
%         rnd=rand();
%         cnd=1;
%         while(sum(fitness(1:cnd))<rnd)
%             cnd=cnd+1;
%         end
%         
%         DeltaX=[DeltaX;C0(cnd,:)];%add cnd to DeltaX;
%         
%         %remove the selected from candidate set and fitness function;
%         C0=removerows(C0,cnd);
%         fitness=removerows(fitness,cnd);
%         
%         rest_candidate=sum(C0,1);
%         
%     end
%     
%     
%     
% %     %crossover and mutate;
% %     i=1;
% %     while(i<size(DeltaX))
% %         if(rand()<p_crossover)
% %             DeltaX(i:i+1,:)=Crossover(DeltaX(i:i+1,:),T,domain);
% %         end
% %         if(rand()<p_mutate)
% %             DeltaX(i,:)=Mutate(DeltaX(i,:),T,domain);
% %         end
% %         if(rand()<p_mutate)
% %             DeltaX(i+1,:)=Mutate(DeltaX(i+1,:),T,domain);
% %         end
% %         i=i+2;
% %     end
% 
%     %remove all points far from max_move
%     center=zeros(2,1);
%     for i=1:size(DeltaX,1)
%         center=center+T*DeltaX(i,:)';
%     end
%     center=center/size(DeltaX,1);
%     
%     DeltaX2=[];
%     for i=1:size(DeltaX,1)
%         if(dist(center,T*DeltaX(i,:)')<=max_move)
%             DeltaX2=[DeltaX2;DeltaX(i,:)];
%         end
%     end
%     DeltaX=DeltaX2;
%     
%     
%     temp2=sum(DeltaX,1);
%     if(temp2(state_no)==0)
%         DeltaX=[DeltaX;true_loc];
%     end
%     
%     state_no_vec=[];
%     for i=1:N
%         if(temp2(i)==1)
%             state_no_vec=[state_no_vec,i];
%         end
%     end
%     return;
    %bool_p=p_prior>delta;
    
    pri0=p_prior;
        last_index=zeros(1,N);
        while(sum(pri0)>1-delta)
            pri1=pri0(pri0>0);
            last_index=(pri0==min(pri1));
            pri0(last_index)=0;
        end
        if(p_prior(last_index)>0)
            pri0(last_index)=1;
        end
    bool_p=pri0>0;
    
end

if setting==2
    for i=1:N
        if p_prior(1,i)~=0
            [coord]=state_to_coordinate(T,i);
            if dist(coord,T*true_loc')<delta
                bool_p(i)=1;
            end
        end
    end
end

if setting==3 || setting==4
    graph_file='Graph_';
    if setting==3
        graph_file=strcat(graph_file,'nb_');
    end
    if setting==4
        graph_file=strcat(graph_file,'cat_');
    end
    graph_file=strcat(graph_file,int2str(delta));
    graph_file=strcat(graph_file,'.txt');
    
    G=load(graph_file);
    for i=1:N
        if p_prior(1,i)~=0
            
            if G(state_no,i)==1
                bool_p(i)=1;
            end
        end
    end
    
end



DeltaX=zeros(sum(bool_p),N);
temp=[];
state_no_vec=[];
for i=1:N
    temp=[temp,bool_p(1,i)];
    row=sum(temp);
    if bool_p(1,i)==1
        DeltaX(row,i)=1;
        state_no_vec=[state_no_vec,i];
    end

end
size_deltax=size(DeltaX,1)

% example of DeltaX: 
%     1     0     0     0     0
%     0     1     0     0     0
%     0     0     0     0     1
% state_no_vec=[1 2 5]';
end



function [d]=dist(x1,x2)
    d=norm(x1-x2);
end

function [coord]=state_to_coordinate(T,state)
    %state=134; coord=[34,2];
    N=size(T,2);
    state_vec=zeros(1,N);
    state_vec(state)=1;
    coord=T*state_vec';
end

function [A_new]=Crossover(A,T,domain)
%A is a 2 x N matrix;
    pos1=T*A(1,:)';
    pos2=T*A(2,:)';
    
    if(rand()>0.5)
        tmp=sign(pos2(1)-pos1(1));
        pos1(1)=pos1(1)+tmp;
        pos2(1)=pos2(1)-tmp;
    end
    
    if(rand()>0.5)
        tmp=sign(pos2(2)-pos1(2));
        pos1(2)=pos1(2)+tmp;
        pos2(2)=pos2(2)-tmp;
    end
    
    state1=pos1(1)+pos1(2)*domain+1;
    state2=pos2(1)+pos2(2)*domain+1;
    

    A_new=zeros(2,domain^2);
    A_new(1,state1)=1;
    A_new(2,state2)=1;
end


function [A_new]=Mutate(A,T,domain)
    pos=T*A(1,:)';
    pos0=pos;
    
    a=rand();
    if(a<0.33)
        pos(1)=pos(1)-1;
    else if(a>0.66)
            pos(1)=pos(1)+1;
        end
    end
    
    a=rand();
    if(a<0.33)
        pos(2)=pos(2)-1;
    else if(a>0.66)
            pos(2)=pos(2)+1;
        end
    end
    
    if(pos(1)<1 || pos(1)>domain)
        pos(1)=pos0(1);
    end
    if(pos(2)<1 || pos(2)>domain)
        pos(2)=pos0(2);
    end
    
    state=pos(1)+pos(2)*domain+1;
    A_new=zeros(1,domain^2);
    A_new(1,state)=1;
    display(size(A_new));


end
